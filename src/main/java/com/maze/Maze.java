package com.maze;

import java.util.ArrayDeque;

/**
 * Representação do mapa: uma grade de tiles onde cada tile é parede ou espaço livre.
 * Coordenadas de tile (x, y) mapeiam para o mundo 3D como (x * TILE, ?, y * TILE).
 */
public final class Maze {

  /** Tamanho de um tile no mundo (unidades). */
  public static final float TILE = 2.0f;
  /** Altura das paredes. */
  public static final float WALL_HEIGHT = 2.6f;

  public final int width;
  public final int height;
  public final int startX, startY;
  public final int exitX, exitY;
  public final long seed;

  private final boolean[][] wall; // [y][x]
  private final int shortestPath;

  Maze(boolean[][] wall, int startX, int startY, int exitX, int exitY, long seed) {
    this.wall = wall;
    this.height = wall.length;
    this.width = wall[0].length;
    this.startX = startX;
    this.startY = startY;
    this.exitX = exitX;
    this.exitY = exitY;
    this.seed = seed;
    this.shortestPath = computeShortestPath();
  }

  /** Fora dos limites conta como parede. */
  public boolean isWall(int x, int y) {
    return x < 0 || y < 0 || x >= width || y >= height || wall[y][x];
  }

  /** Menor caminho (em tiles) entre entrada e saída, ou -1 se não houver. */
  public int shortestPath() {
    return shortestPath;
  }

  public float centerX(int tx) {
    return (tx + 0.5f) * TILE;
  }

  public float centerZ(int ty) {
    return (ty + 0.5f) * TILE;
  }

  private int computeShortestPath() {
    int[][] dist = new int[height][width];
    for (int[] row : dist) java.util.Arrays.fill(row, -1);
    ArrayDeque<int[]> queue = new ArrayDeque<>();
    dist[startY][startX] = 0;
    queue.add(new int[] {startX, startY});
    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};
    while (!queue.isEmpty()) {
      int[] c = queue.poll();
      if (c[0] == exitX && c[1] == exitY) return dist[c[1]][c[0]];
      for (int d = 0; d < 4; d++) {
        int nx = c[0] + dx[d], ny = c[1] + dy[d];
        if (!isWall(nx, ny) && dist[ny][nx] < 0) {
          dist[ny][nx] = dist[c[1]][c[0]] + 1;
          queue.add(new int[] {nx, ny});
        }
      }
    }
    return -1;
  }
}
