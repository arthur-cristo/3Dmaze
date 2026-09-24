package com.maze;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Janela, loop principal, máquina de estados (menu / jogando / pausa / vitória),
 * entrada, cronômetro e pontuação.
 */
public final class Game {

  private enum State { MENU, PLAYING, PAUSED, WON }

  /** Altura da tela "virtual" do HUD; a largura acompanha a proporção da janela. */
  private static final float VH = 720f;
  private static final float MAX_DT = 0.05f;

  private final Long fixedSeed;
  private final Random seedRng = new Random();
  private MazeSize size;

  private long window;
  private int winW = 1280, winH = 720;
  private int fbW = 1280, fbH = 720;

  private State state = State.MENU;
  private SceneRenderer scene;
  private Hud hud;
  private Menu mainMenu, pauseMenu, winMenu;

  private Maze maze;
  private Player player;
  private double elapsed;
  private boolean timerRunning;
  private int finalScore;
  private boolean newRecord;
  private final EnumMap<MazeSize, Double> bestTime = new EnumMap<>(MazeSize.class);
  private final EnumMap<MazeSize, Integer> bestScore = new EnumMap<>(MazeSize.class);

  private double mouseX, mouseY, lastMouseX, lastMouseY;
  private boolean mouseInit;

  private boolean showDebug;
  private float avgFrame = 1f / 60f;

  public Game(Long fixedSeed, MazeSize size) {
    this.fixedSeed = fixedSeed;
    this.size = size;
  }

  // ---------------------------------------------------------------- ciclo de vida

  public void run() {
    try {
      init();
      loop();
    } finally {
      shutdown();
    }
  }

  private void init() {
    GLFWErrorCallback.createPrint(System.err).set();
    if (!glfwInit()) throw new IllegalStateException("Não foi possível inicializar o GLFW");

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
    glfwWindowHint(GLFW_SAMPLES, 4);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    window = glfwCreateWindow(winW, winH, "3D Maze", NULL, NULL);
    if (window == NULL) throw new RuntimeException("Falha ao criar a janela GLFW");

    GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
    if (vm != null) {
      glfwSetWindowPos(window, (vm.width() - winW) / 2, (vm.height() - winH) / 2);
    }

    glfwSetKeyCallback(window, this::onKey);
    glfwSetMouseButtonCallback(window, this::onMouseButton);
    glfwSetCursorPosCallback(window, this::onCursor);
    glfwSetWindowSizeCallback(window, (w, width, height) -> { winW = width; winH = height; });
    glfwSetFramebufferSizeCallback(window, (w, width, height) -> { fbW = width; fbH = height; });
    glfwSetWindowFocusCallback(window, (w, focused) -> {
      if (!focused && state == State.PLAYING) pause();
    });

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1); // vsync
    GL.createCapabilities();
    glEnable(GL_MULTISAMPLE);

    int[] a = new int[1], b = new int[1];
    glfwGetFramebufferSize(window, a, b);
    fbW = a[0];
    fbH = b[0];
    glfwGetWindowSize(window, a, b);
    winW = a[0];
    winH = b[0];

    System.out.println("OpenGL: " + glGetString(GL_VERSION));

    scene = new SceneRenderer();
    hud = new Hud();
    buildMenus();
    setState(State.MENU);
  }

  private void loop() {
    double last = glfwGetTime();
    while (!glfwWindowShouldClose(window)) {
      double now = glfwGetTime();
      float raw = (float) (now - last);
      last = now;
      avgFrame += (raw - avgFrame) * 0.05f;

      glfwPollEvents();

      if (fbW <= 0 || fbH <= 0) { // janela minimizada
        glfwWaitEventsTimeout(0.1);
        continue;
      }

      update(raw);
      render(now);
      glfwSwapBuffers(window);
    }
  }

  private void shutdown() {
    if (hud != null) hud.close();
    if (scene != null) scene.close();
    if (window != NULL) {
      Callbacks.glfwFreeCallbacks(window);
      glfwDestroyWindow(window);
    }
    glfwTerminate();
    var cb = glfwSetErrorCallback(null);
    if (cb != null) cb.free();
    System.out.println("Jogo encerrado.");
  }

  // ---------------------------------------------------------------- estados e partida

  private void buildMenus() {
    mainMenu = new Menu(300f)
        .add(() -> "PLAY", this::startGame)
        .addAdjustable(() -> "TAMANHO: < " + size.label + " >", () -> cycleSize(1), this::cycleSize)
        .add(() -> "SAIR", () -> glfwSetWindowShouldClose(window, true));

    pauseMenu = new Menu(300f)
        .add(() -> "CONTINUAR", () -> setState(State.PLAYING))
        .add(() -> "REINICIAR", this::startGame)
        .add(() -> "MENU PRINCIPAL", () -> setState(State.MENU));

    winMenu = new Menu(440f)
        .add(() -> "JOGAR NOVAMENTE", this::startGame)
        .add(() -> "MENU PRINCIPAL", () -> setState(State.MENU));
  }

  private void cycleSize(int dir) {
    MazeSize[] all = MazeSize.values();
    size = all[(size.ordinal() + dir + all.length) % all.length];
  }

  private Menu activeMenu() {
    return switch (state) {
      case MENU -> mainMenu;
      case PAUSED -> pauseMenu;
      case WON -> winMenu;
      case PLAYING -> null;
    };
  }

  private void setState(State s) {
    state = s;
    mouseInit = false;
    boolean grab = s == State.PLAYING;
    glfwSetInputMode(window, GLFW_CURSOR, grab ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    if (grab && glfwRawMouseMotionSupported()) {
      glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
    }
    Menu m = activeMenu();
    if (m != null) m.selected = 0;
  }

  private void pause() {
    setState(State.PAUSED);
  }

  /** Gera um mapa novo, posiciona o jogador na entrada e zera cronômetro/pontuação. */
  private void startGame() {
    long seed = fixedSeed != null ? fixedSeed : seedRng.nextLong();
    maze = MazeGenerator.generate(size.cells, size.cells, seed);
    scene.setMaze(maze);
    player = Player.atEntrance(maze);
    elapsed = 0;
    timerRunning = false;
    newRecord = false;
    System.out.printf(Locale.ROOT, "Novo labirinto: tamanho=%s (%dx%d tiles) seed=%d caminho minimo=%d%n",
        size.name(), maze.width, maze.height, seed, maze.shortestPath());
    setState(State.PLAYING);
  }

  private int computeScore(double time) {
    int base = maze.shortestPath() * 10;
    double par = maze.shortestPath() * Maze.TILE / Player.WALK_SPEED * 1.6; // tempo "de referência"
    int bonus = (int) Math.round(Math.max(0.0, par - time) * 25.0);
    return base + bonus;
  }

  private void win() {
    finalScore = computeScore(elapsed);
    Integer prevScore = bestScore.get(size);
    Double prevTime = bestTime.get(size);
    newRecord = prevScore == null || finalScore > prevScore;
    if (newRecord) bestScore.put(size, finalScore);
    if (prevTime == null || elapsed < prevTime) bestTime.put(size, elapsed);
    System.out.printf(Locale.ROOT, "Vitoria! tempo=%s pontos=%d%n", formatTime(elapsed), finalScore);
    setState(State.WON);
  }

  // ---------------------------------------------------------------- entrada

  private boolean down(int key) {
    return glfwGetKey(window, key) == GLFW_PRESS;
  }

  private void onKey(long w, int key, int scancode, int action, int mods) {
    if (action == GLFW_RELEASE) return;
    boolean press = action == GLFW_PRESS;

    if (key == GLFW_KEY_F3 && press) {
      showDebug = !showDebug;
      return;
    }

    if (state == State.PLAYING) {
      if (key == GLFW_KEY_ESCAPE && press) pause();
      return;
    }

    Menu m = activeMenu();
    switch (key) {
      case GLFW_KEY_ESCAPE -> {
        if (!press) return;
        switch (state) {
          case MENU -> glfwSetWindowShouldClose(window, true);
          case PAUSED -> setState(State.PLAYING);
          default -> setState(State.MENU);
        }
      }
      case GLFW_KEY_UP, GLFW_KEY_W -> m.move(-1);
      case GLFW_KEY_DOWN, GLFW_KEY_S -> m.move(1);
      case GLFW_KEY_LEFT, GLFW_KEY_A -> m.adjust(-1);
      case GLFW_KEY_RIGHT, GLFW_KEY_D -> m.adjust(1);
      case GLFW_KEY_ENTER, GLFW_KEY_KP_ENTER, GLFW_KEY_SPACE -> {
        if (press) m.activate();
      }
      default -> { }
    }
  }

  private float virtualWidth() {
    return VH * fbW / Math.max(1, fbH);
  }

  /** Converte coordenadas de janela (mouse) para o espaço virtual do HUD. */
  private float toVirtualX(double x) {
    return (float) (x / Math.max(1, winW) * virtualWidth());
  }

  private float toVirtualY(double y) {
    return (float) (y / Math.max(1, winH) * VH);
  }

  private void onCursor(long w, double x, double y) {
    if (state == State.PLAYING) {
      if (mouseInit && player != null) {
        player.look((float) (x - lastMouseX), (float) (y - lastMouseY));
      }
      lastMouseX = x;
      lastMouseY = y;
      mouseInit = true;
    } else {
      mouseX = x;
      mouseY = y;
      Menu m = activeMenu();
      if (m != null) {
        int i = m.hit(toVirtualX(x), toVirtualY(y), virtualWidth());
        if (i >= 0) m.selected = i;
      }
    }
  }

  private void onMouseButton(long w, int button, int action, int mods) {
    if (button != GLFW_MOUSE_BUTTON_LEFT || action != GLFW_PRESS) return;
    Menu m = activeMenu();
    if (m == null) return;
    int i = m.hit(toVirtualX(mouseX), toVirtualY(mouseY), virtualWidth());
    if (i >= 0) {
      m.selected = i;
      m.activate();
    }
  }

  // ---------------------------------------------------------------- Movimentação

  private void update(float rawDt) {
    if (state != State.PLAYING) return;

    float forward = 0, strafe = 0, turn = 0;
    if (down(GLFW_KEY_W) || down(GLFW_KEY_UP)) forward += 1;
    if (down(GLFW_KEY_S) || down(GLFW_KEY_DOWN)) forward -= 1;
    if (down(GLFW_KEY_D) || down(GLFW_KEY_RIGHT)) strafe += 1;
    if (down(GLFW_KEY_A) || down(GLFW_KEY_LEFT)) strafe -= 1;
    boolean sprint = down(GLFW_KEY_LEFT_SHIFT) || down(GLFW_KEY_RIGHT_SHIFT);

    if (!timerRunning && (forward != 0 || strafe != 0)) timerRunning = true;

    player.update(Math.min(rawDt, MAX_DT), forward, strafe, turn, sprint, maze);
    if (timerRunning) elapsed += rawDt;

    if (player.tileX() == maze.exitX && player.tileZ() == maze.exitY) win();
  }

  // ---------------------------------------------------------------- desenho

  private void render(double time) {
    glViewport(0, 0, fbW, fbH);
    glClearColor(0f, 0f, 0f, 1f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (state != State.MENU && maze != null) {
      scene.render(player, (float) fbW / fbH, (float) time);
    }

    float vw = virtualWidth();
    hud.begin(vw, VH);
    switch (state) {
      case MENU -> drawMainMenu(vw);
      case PLAYING -> drawHud(vw);
      case PAUSED -> {
        drawHud(vw);
        drawPause(vw);
      }
      case WON -> drawWin(vw);
    }
    if (showDebug) drawDebug();
    hud.end();
  }

  private void drawMainMenu(float vw) {
    hud.textCentered("3D MAZE", vw / 2f, 110f, 14f, 1f, 1f);
    hud.textCentered("ENCONTRE A SAÍDA", vw / 2f, 230f, 4f, 0.6f, 1f);
    mainMenu.draw(hud, vw);
    hud.textCentered("SETAS / WASD + ENTER   OU   MOUSE", vw / 2f, VH - 60f, 3f, 0.5f, 1f);
    if (fixedSeed != null) {
      hud.textCentered("SEED FIXA: " + fixedSeed, vw / 2f, VH - 34f, 2.5f, 0.4f, 1f);
    }
  }

  private void drawHud(float vw) {
    float m = 28f;
    hud.text("TEMPO", m, m, 3f, 0.65f, 1f);
    hud.text(formatTime(elapsed), m, m + 28f, 5f, 1f, 1f);

    hud.textRight("PONTUAÇÃO", vw - m, m, 3f, 0.65f, 1f);
    hud.textRight(String.valueOf(computeScore(elapsed)), vw - m, m + 28f, 5f, 1f, 1f);

    if (state == State.PLAYING && !timerRunning) {
      hud.textCentered("WASD / SETAS: MOVER   MOUSE: OLHAR   SHIFT: CORRER   ESC: PAUSA",
          vw / 2f, VH - 60f, 3f, 1f, 0.85f, 0.1f, 1f);   // amarelo
      hud.textCentered("O TEMPO COMEÇA QUANDO VOCE SE MOVER",
          vw / 2f, VH - 34f, 3f, 1f, 0.85f, 0.1f, 1f);   // amarelo
    }
  }

  private void drawPause(float vw) {
    hud.rect(0, 0, vw, VH, 0f, 0.65f);
    hud.textCentered("PAUSADO", vw / 2f, 130f, 12f, 1f, 1f);
    pauseMenu.draw(hud, vw);
  }

  private void drawWin(float vw) {
    hud.rect(0, 0, vw, VH, 0f, 0.75f);
    hud.textCentered("VITÓRIA!", vw / 2f, 60f, 14f, 1f, 1f);
    hud.textCentered("TEMPO " + formatTime(elapsed), vw / 2f, 210f, 6f, 1f, 1f);
    hud.textCentered("PONTUAÇÃO " + finalScore, vw / 2f, 268f, 6f, 1f, 1f);

    Double bt = bestTime.get(size);
    Integer bs = bestScore.get(size);
    if (newRecord) {
      hud.textCentered("NOVO RECORDE!", vw / 2f, 340f, 5f, 1f, 1f);
    } else if (bt != null && bs != null) {
      hud.textCentered("MELHOR: " + bs + " PONTOS  " + formatTime(bt), vw / 2f, 340f, 3.5f, 0.7f, 1f);
    }
    winMenu.draw(hud, vw);
  }

  private void drawDebug() {
    float fps = 1f / Math.max(avgFrame, 1e-6f);
    String s = String.format(Locale.ROOT, "FPS %.0f  FRAME %.2f MS  SEED %s  MAPA %s  VERTICES %d",
        fps, avgFrame * 1000f,
        maze != null ? String.valueOf(maze.seed) : "-",
        maze != null ? maze.width + "X" + maze.height : "-",
        scene.vertexCount());
    hud.text(s, 12f, VH - 22f, 2f, 0.9f, 1f);
  }

  private static String formatTime(double t) {
    int min = (int) (t / 60);
    double sec = t - min * 60;
    return String.format(Locale.ROOT, "%02d:%04.1f", min, sec);
  }
}
