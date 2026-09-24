package com.maze;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

/**
 * Camada 2D: retângulos e texto com fonte bitmap 5x7 embutida (sem dependências extras).
 * Tudo em tons de cinza, com coordenadas "virtuais" de altura fixa (origem no canto superior esquerdo).
 */
final class Hud implements AutoCloseable {

  private static final Map<Character, int[]> FONT = new HashMap<>();

  /** Linhas (5 bits) do glifo de um caractere, ou null se não existir. */
  static int[] glyphRows(char c) {
    return FONT.get(c);
  }

  private static void def(char c, int... rows) {
    FONT.put(c, rows);
  }

  static {
    def('A', 0x0E, 0x11, 0x11, 0x1F, 0x11, 0x11, 0x11);
    def('B', 0x1E, 0x11, 0x11, 0x1E, 0x11, 0x11, 0x1E);
    def('C', 0x0E, 0x11, 0x10, 0x10, 0x10, 0x11, 0x0E);
    def('D', 0x1E, 0x11, 0x11, 0x11, 0x11, 0x11, 0x1E);
    def('E', 0x1F, 0x10, 0x10, 0x1E, 0x10, 0x10, 0x1F);
    def('F', 0x1F, 0x10, 0x10, 0x1E, 0x10, 0x10, 0x10);
    def('G', 0x0E, 0x11, 0x10, 0x17, 0x11, 0x11, 0x0F);
    def('H', 0x11, 0x11, 0x11, 0x1F, 0x11, 0x11, 0x11);
    def('I', 0x0E, 0x04, 0x04, 0x04, 0x04, 0x04, 0x0E);
    def('J', 0x07, 0x02, 0x02, 0x02, 0x02, 0x12, 0x0C);
    def('K', 0x11, 0x12, 0x14, 0x18, 0x14, 0x12, 0x11);
    def('L', 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x1F);
    def('M', 0x11, 0x1B, 0x15, 0x15, 0x11, 0x11, 0x11);
    def('N', 0x11, 0x11, 0x19, 0x15, 0x13, 0x11, 0x11);
    def('O', 0x0E, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0E);
    def('P', 0x1E, 0x11, 0x11, 0x1E, 0x10, 0x10, 0x10);
    def('Q', 0x0E, 0x11, 0x11, 0x11, 0x15, 0x12, 0x0D);
    def('R', 0x1E, 0x11, 0x11, 0x1E, 0x14, 0x12, 0x11);
    def('S', 0x0F, 0x10, 0x10, 0x0E, 0x01, 0x01, 0x1E);
    def('T', 0x1F, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04);
    def('U', 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0E);
    def('V', 0x11, 0x11, 0x11, 0x11, 0x11, 0x0A, 0x04);
    def('W', 0x11, 0x11, 0x11, 0x15, 0x15, 0x1B, 0x11);
    def('X', 0x11, 0x11, 0x0A, 0x04, 0x0A, 0x11, 0x11);
    def('Y', 0x11, 0x11, 0x0A, 0x04, 0x04, 0x04, 0x04);
    def('Z', 0x1F, 0x01, 0x02, 0x04, 0x08, 0x10, 0x1F);
    def('0', 0x0E, 0x11, 0x13, 0x15, 0x19, 0x11, 0x0E);
    def('1', 0x04, 0x0C, 0x04, 0x04, 0x04, 0x04, 0x0E);
    def('2', 0x0E, 0x11, 0x01, 0x02, 0x04, 0x08, 0x1F);
    def('3', 0x1F, 0x02, 0x04, 0x02, 0x01, 0x11, 0x0E);
    def('4', 0x02, 0x06, 0x0A, 0x12, 0x1F, 0x02, 0x02);
    def('5', 0x1F, 0x10, 0x1E, 0x01, 0x01, 0x11, 0x0E);
    def('6', 0x06, 0x08, 0x10, 0x1E, 0x11, 0x11, 0x0E);
    def('7', 0x1F, 0x01, 0x02, 0x04, 0x08, 0x08, 0x08);
    def('8', 0x0E, 0x11, 0x11, 0x0E, 0x11, 0x11, 0x0E);
    def('9', 0x0E, 0x11, 0x11, 0x0F, 0x01, 0x02, 0x0C);
    def('.', 0x00, 0x00, 0x00, 0x00, 0x00, 0x0C, 0x0C);
    def(':', 0x00, 0x0C, 0x0C, 0x00, 0x0C, 0x0C, 0x00);
    def('-', 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00);
    def('+', 0x00, 0x04, 0x04, 0x1F, 0x04, 0x04, 0x00);
    def('!', 0x04, 0x04, 0x04, 0x04, 0x04, 0x00, 0x04);
    def('?', 0x0E, 0x11, 0x01, 0x02, 0x04, 0x00, 0x04);
    def('/', 0x01, 0x01, 0x02, 0x04, 0x08, 0x10, 0x10);
    def(',', 0x00, 0x00, 0x00, 0x00, 0x0C, 0x04, 0x08);
    def('<', 0x02, 0x04, 0x08, 0x10, 0x08, 0x04, 0x02);
    def('>', 0x08, 0x04, 0x02, 0x01, 0x02, 0x04, 0x08);
    def('=', 0x00, 0x00, 0x1F, 0x00, 0x1F, 0x00, 0x00);
    def('%', 0x18, 0x19, 0x02, 0x04, 0x08, 0x13, 0x03);
  }

  private static final String VERTEX = """
      #version 330 core
      layout(location = 0) in vec2 aPos;
      layout(location = 1) in vec4 aColor;
      uniform vec2 uSize;
      out vec4 vColor;
      void main() {
        vec2 p = aPos / uSize * 2.0 - 1.0;
        gl_Position = vec4(p.x, -p.y, 0.0, 1.0);
        vColor = aColor;
      }
      """;

  private static final String FRAGMENT = """
      #version 330 core
      in vec4 vColor;
      out vec4 FragColor;
      void main() {
        FragColor = vColor;
      }
      """;

  private static final int FLOATS_PER_VERTEX = 6;

  private final Shader shader = new Shader(VERTEX, FRAGMENT);
  private final int vao;
  private final int vbo;
  private FloatBuffer buf = BufferUtils.createFloatBuffer(1 << 16);
  private float vw, vh;

  Hud() {
    vao = glGenVertexArrays();
    vbo = glGenBuffers();
    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    int stride = FLOATS_PER_VERTEX * Float.BYTES;
    glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 4, GL_FLOAT, false, stride, 2L * Float.BYTES);
    glEnableVertexAttribArray(1);
    glBindVertexArray(0);
  }

  void begin(float virtualWidth, float virtualHeight) {
    vw = virtualWidth;
    vh = virtualHeight;
    buf.clear();
  }

  /** Retângulo preenchido; g = tom de cinza (0..1), a = opacidade. */
  void rect(float x, float y, float w, float h, float g, float a) {
    rect(x, y, w, h, g, g, g, a);
  }

  /** Retângulo preenchido com cor RGB (0..1). */
  void rect(float x, float y, float w, float h, float r, float g, float b, float a) {
    ensure(6 * FLOATS_PER_VERTEX);
    vertex(x, y, r, g, b, a);
    vertex(x + w, y, r, g, b, a);
    vertex(x + w, y + h, r, g, b, a);
    vertex(x, y, r, g, b, a);
    vertex(x + w, y + h, r, g, b, a);
    vertex(x, y + h, r, g, b, a);
  }

  /** Contorno de retângulo com espessura t. */
  void frame(float x, float y, float w, float h, float t, float g, float a) {
    rect(x, y, w, t, g, a);
    rect(x, y + h - t, w, t, g, a);
    rect(x, y + t, t, h - 2 * t, g, a);
    rect(x + w - t, y + t, t, h - 2 * t, g, a);
  }

  float textWidth(String s, float ps) {
    return s.isEmpty() ? 0 : s.length() * 6 * ps - ps;
  }

  void text(String s, float x, float y, float ps, float g, float a) {
    text(s, x, y, ps, g, g, g, a);
  }

  /** Texto colorido (RGB 0..1). */
  void text(String s, float x, float y, float ps, float r, float g, float b, float a) {
    s = s.toUpperCase(Locale.ROOT);
    if (Math.max(r, Math.max(g, b)) > 0.5f) { // sombra
      drawString(s, x + ps * 0.7f, y + ps * 0.7f, ps, 0f, 0f, 0f, a * 0.6f);
    }
    drawString(s, x, y, ps, r, g, b, a);
  }

  void textCentered(String s, float cx, float y, float ps, float g, float a) {
    text(s, cx - textWidth(s, ps) / 2f, y, ps, g, a);
  }

  void textCentered(String s, float cx, float y, float ps, float r, float g, float b, float a) {
    text(s, cx - textWidth(s, ps) / 2f, y, ps, r, g, b, a);
  }

  void textRight(String s, float rx, float y, float ps, float g, float a) {
    text(s, rx - textWidth(s, ps), y, ps, g, a);
  }

  private void drawString(String s, float x, float y, float ps, float r, float g, float b, float a) {
    float cx = x;
    for (int i = 0; i < s.length(); i++) {
      glyph(s.charAt(i), cx, y, ps, r, g, b, a);
      cx += 6 * ps;
    }
  }

  private void glyph(char c, float x, float y, float ps, float r, float g, float b, float a) {
    char base = c;
    int accent = 0; // 1 agudo, 2 til, 3 cedilha
    switch (c) {
      case 'Á': base = 'A'; accent = 1; break;
      case 'É': base = 'E'; accent = 1; break;
      case 'Í': base = 'I'; accent = 1; break;
      case 'Ó': base = 'O'; accent = 1; break;
      case 'Ú': base = 'U'; accent = 1; break;
      case 'Ã': base = 'A'; accent = 2; break;
      case 'Ç': base = 'C'; accent = 3; break;
      default: break;
    }
    int[] rows = FONT.get(base);
    if (rows == null) return;
    for (int row = 0; row < 7; row++) {
      for (int col = 0; col < 5; col++) {
        if (((rows[row] >> (4 - col)) & 1) != 0) {
          rect(x + col * ps, y + row * ps, ps, ps, r, g, b, a);
        }
      }
    }
    switch (accent) {
      case 1 -> { px(x, y, ps, 3, -2, r, g, b, a); px(x, y, ps, 2, -1, r, g, b, a); }
      case 2 -> { px(x, y, ps, 1, -1, r, g, b, a); px(x, y, ps, 2, -2, r, g, b, a); px(x, y, ps, 3, -1, r, g, b, a); }
      case 3 -> { px(x, y, ps, 2, 7, r, g, b, a); px(x, y, ps, 1, 8, r, g, b, a); }
      default -> { }
    }
  }

  private void px(float x, float y, float ps, int col, int row, float r, float g, float b, float a) {
    rect(x + col * ps, y + row * ps, ps, ps, r, g, b, a);
  }

  private void vertex(float x, float y, float r, float g, float b, float a) {
    buf.put(x).put(y).put(r).put(g).put(b).put(a);
  }

  private void ensure(int floats) {
    if (buf.remaining() >= floats) return;
    FloatBuffer bigger = BufferUtils.createFloatBuffer(Math.max(buf.capacity() * 2, buf.position() + floats));
    buf.flip();
    bigger.put(buf);
    buf = bigger;
  }

  /** Envia tudo o que foi desenhado desde begin() para a GPU. */
  void end() {
    if (buf.position() == 0) return;
    buf.flip();

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    shader.use();
    shader.setVec2("uSize", vw, vh);
    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);
    glDrawArrays(GL_TRIANGLES, 0, buf.limit() / FLOATS_PER_VERTEX);
    glBindVertexArray(0);

    glDisable(GL_BLEND);
    buf.clear();
  }

  @Override
  public void close() {
    glDeleteBuffers(vbo);
    glDeleteVertexArrays(vao);
    shader.close();
  }
}
