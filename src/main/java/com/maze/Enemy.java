package com.maze;

/**
 * Inimigo perseguidor. Segue o menor caminho (BFS na grade de tiles) até o jogador, então
 * nunca atravessa paredes
 *
 */
public final class Enemy {

  /** Velocidade (unidades/s). O jogador anda a 3.4 e corre a 5.6, então dá para fugir. */
  public static final float SPEED = 3.2f * 1.10f * 1.10f * 1.20f; // ~3.48
  /** Segundos, depois que o jogador começa a se mover, até o inimigo aparecer (0 = imediato). */
  public static final float SPAWN_DELAY = 0f;
  /**
   * Como ele nasce na entrada, junto do jogador, só pode encostar depois deste tempo;
   * assim o jogador consegue sair do lugar e a perseguição começa de verdade.
   */
  public static final float CATCH_GRACE = 1.5f;
  /** Distância do centro do jogador em que o inimigo "encosta". */
  public static final float CATCH_DISTANCE = Player.RADIUS + 0.35f;

  public float x, z;

  private int[][] dist;          // distância (em tiles) de cada tile até o tile do jogador
  private int distTargetX = -1, distTargetZ = -1;
  private float age;

  private Enemy() {}

  /** Nasce na entrada do labirinto, atrás do jogador. */
  public static Enemy spawnAtEntrance(Maze maze) {
    Enemy e = new Enemy();
    e.x = maze.centerX(maze.startX);
    e.z = maze.centerZ(maze.startY);
    return e;
  }

  public void update(float dt, Player player, Maze maze) {
    age += dt;
    int ptx = player.tileX(), ptz = player.tileZ();
    if (dist == null || ptx != distTargetX || ptz != distTargetZ) {
      computeDistances(maze, ptx, ptz);
    }

    int etx = (int) Math.floor(x / Maze.TILE), etz = (int) Math.floor(z / Maze.TILE);
    float tx, tz;
    if (etx == ptx && etz == ptz) { // mesmo tile: vai direto ao jogador
      tx = player.x;
      tz = player.z;
    } else {                         // senão, vai ao centro do vizinho mais próximo do jogador
      int best = dist[etz][etx];
      int bx = etx, bz = etz;
      int[] dx = {1, -1, 0, 0};
      int[] dz = {0, 0, 1, -1};
      for (int d = 0; d < 4; d++) {
        int nx = etx + dx[d], nz = etz + dz[d];
        if (maze.isWall(nx, nz)) continue;
        int nd = dist[nz][nx];
        if (nd >= 0 && (best < 0 || nd < best)) {
          best = nd;
          bx = nx;
          bz = nz;
        }
      }
      tx = maze.centerX(bx);
      tz = maze.centerZ(bz);
    }

    float ddx = tx - x, ddz = tz - z;
    float len = (float) Math.hypot(ddx, ddz);
    float step = SPEED * dt;
    if (len <= step) {
      x = tx;
      z = tz;
    } else {
      x += ddx / len * step;
      z += ddz / len * step;
    }
  }

  public boolean hasCaught(Player player) {
    return age >= CATCH_GRACE && Math.hypot(player.x - x, player.z - z) < CATCH_DISTANCE;
  }

  /** BFS a partir do tile do jogador. */
  private void computeDistances(Maze maze, int tx, int tz) {
    if (dist == null) dist = new int[maze.height][maze.width];
    for (int[] row : dist) java.util.Arrays.fill(row, -1);
    distTargetX = tx;
    distTargetZ = tz;
    if (maze.isWall(tx, tz)) return;

    int[] queue = new int[maze.width * maze.height];
    int head = 0, tail = 0;
    dist[tz][tx] = 0;
    queue[tail++] = tz * maze.width + tx;
    int[] dx = {1, -1, 0, 0};
    int[] dz = {0, 0, 1, -1};
    while (head < tail) {
      int c = queue[head++];
      int cx = c % maze.width, cz = c / maze.width;
      for (int d = 0; d < 4; d++) {
        int nx = cx + dx[d], nz = cz + dz[d];
        if (!maze.isWall(nx, nz) && dist[nz][nx] < 0) {
          dist[nz][nx] = dist[cz][cx] + 1;
          queue[tail++] = nz * maze.width + nx;
        }
      }
    }
  }
}
