package com.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Geração procedural: backtracker recursivo (iterativo) sobre células, expandido para uma
 * grade de tiles (2n+1). Depois remove algumas paredes internas para criar ciclos, e abre
 * entrada (topo) e saída (base). Como o backtracker gera uma árvore geradora, sempre existe
 * caminho; a validação por BFS no construtor de {@link Maze} garante isso.
 */
public final class MazeGenerator {

  /** Fração das paredes internas restantes que serão removidas para criar atalhos. */
  private static final double LOOP_FACTOR = 0.06;

  private MazeGenerator() {}

  public static Maze generate(int cellsX, int cellsY, long seed) {
    if (cellsX < 2 || cellsY < 2) {
      throw new IllegalArgumentException("O labirinto precisa de pelo menos 2x2 células");
    }
    int w = cellsX * 2 + 1;
    int h = cellsY * 2 + 1;
    boolean[][] wall = new boolean[h][w];
    for (boolean[] row : wall) Arrays.fill(row, true);

    Random rng = new Random(seed);
    boolean[][] visited = new boolean[cellsY][cellsX];
    int[] stack = new int[cellsX * cellsY];
    int sp = 0;

    int sx = rng.nextInt(cellsX), sy = rng.nextInt(cellsY);
    visited[sy][sx] = true;
    wall[2 * sy + 1][2 * sx + 1] = false;
    stack[sp++] = sy * cellsX + sx;

    int[] dx = {1, -1, 0, 0};
    int[] dy = {0, 0, 1, -1};
    int[] options = new int[4];

    while (sp > 0) {
      int cur = stack[sp - 1];
      int cx = cur % cellsX, cy = cur / cellsX;
      int n = 0;
      for (int d = 0; d < 4; d++) {
        int nx = cx + dx[d], ny = cy + dy[d];
        if (nx >= 0 && ny >= 0 && nx < cellsX && ny < cellsY && !visited[ny][nx]) {
          options[n++] = d;
        }
      }
      if (n == 0) {
        sp--;
        continue;
      }
      int d = options[rng.nextInt(n)];
      int nx = cx + dx[d], ny = cy + dy[d];
      wall[2 * cy + 1 + dy[d]][2 * cx + 1 + dx[d]] = false; // parede entre as células
      wall[2 * ny + 1][2 * nx + 1] = false;
      visited[ny][nx] = true;
      stack[sp++] = ny * cellsX + nx;
    }

    // Ciclos: paredes que separam duas células (x e y com paridades diferentes).
    List<int[]> candidates = new ArrayList<>();
    for (int y = 1; y < h - 1; y++) {
      for (int x = 1; x < w - 1; x++) {
        if (wall[y][x] && ((x & 1) != (y & 1))) candidates.add(new int[] {x, y});
      }
    }
    Collections.shuffle(candidates, rng);
    int toRemove = Math.max(1, (int) (candidates.size() * LOOP_FACTOR));
    for (int i = 0; i < toRemove && i < candidates.size(); i++) {
      int[] c = candidates.get(i);
      wall[c[1]][c[0]] = false;
    }

    // Entrada no topo (canto superior esquerdo), saída na base (canto inferior direito).
    int startX = 1, startY = 0;
    int exitX = w - 2, exitY = h - 1;
    wall[startY][startX] = false;
    wall[exitY][exitX] = false;

    Maze maze = new Maze(wall, startX, startY, exitX, exitY, seed);
    if (maze.shortestPath() < 0) {
      throw new IllegalStateException("Labirinto sem solução (seed " + seed + ")");
    }
    return maze;
  }
}
