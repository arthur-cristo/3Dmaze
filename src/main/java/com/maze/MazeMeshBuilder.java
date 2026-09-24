package com.maze;

import java.util.Arrays;

/**
 * Gera a geometria do labirinto por código: chão, teto e paredes. Só cria as faces visíveis
 * (paredes voltadas para tiles livres), então a malha fica pequena mesmo em mapas grandes.
 *
 * Vértice: posição(3) + normal(3) + material(1). Materiais: 0 parede, 1 chão, 2 teto, 3 saída.
 */
final class MazeMeshBuilder {

  static final float MAT_WALL = 0, MAT_FLOOR = 1, MAT_CEILING = 2, MAT_EXIT = 3;

  private float[] data = new float[1 << 16];
  private int len;

  static Mesh build(Maze maze) {
    MazeMeshBuilder b = new MazeMeshBuilder();
    b.generate(maze);
    return new Mesh(b.data, b.len, 3, 3, 1);
  }

  private void generate(Maze m) {
    final float t = Maze.TILE, h = Maze.WALL_HEIGHT;
    for (int ty = 0; ty < m.height; ty++) {
      for (int tx = 0; tx < m.width; tx++) {
        if (m.isWall(tx, ty)) continue;

        float x0 = tx * t, x1 = x0 + t, z0 = ty * t, z1 = z0 + t;
        boolean isExit = tx == m.exitX && ty == m.exitY;

        // Chão e teto
        quad(x0, 0, z0, x1, 0, z0, x1, 0, z1, x0, 0, z1, 0, 1, 0, isExit ? MAT_EXIT : MAT_FLOOR);
        quad(x0, h, z0, x1, h, z0, x1, h, z1, x0, h, z1, 0, -1, 0, MAT_CEILING);

        // Paredes (face voltada para este tile)
        if (m.isWall(tx - 1, ty)) {
          quad(x0, 0, z0, x0, 0, z1, x0, h, z1, x0, h, z0, 1, 0, 0, MAT_WALL);
        }
        if (m.isWall(tx + 1, ty)) {
          quad(x1, 0, z0, x1, h, z0, x1, h, z1, x1, 0, z1, -1, 0, 0, MAT_WALL);
        }
        if (m.isWall(tx, ty - 1)) {
          quad(x0, 0, z0, x0, h, z0, x1, h, z0, x1, 0, z0, 0, 0, 1, MAT_WALL);
        }
        if (m.isWall(tx, ty + 1)) {
          // Parede do fundo da saída brilha, marcando o objetivo.
          quad(x0, 0, z1, x1, 0, z1, x1, h, z1, x0, h, z1, 0, 0, -1, isExit ? MAT_EXIT : MAT_WALL);
        }
      }
    }
  }

  /** Emite um quad; a ordem dos triângulos é corrigida para que a normal aponte para a face frontal. */
  private void quad(float x0, float y0, float z0, float x1, float y1, float z1,
                    float x2, float y2, float z2, float x3, float y3, float z3,
                    float nx, float ny, float nz, float mat) {
    float ax = x1 - x0, ay = y1 - y0, az = z1 - z0;
    float bx = x2 - x0, by = y2 - y0, bz = z2 - z0;
    float cx = ay * bz - az * by, cy = az * bx - ax * bz, cz = ax * by - ay * bx;
    boolean flip = cx * nx + cy * ny + cz * nz < 0;

    float[] v = {x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3};
    int[] order = flip ? new int[] {0, 2, 1, 0, 3, 2} : new int[] {0, 1, 2, 0, 2, 3};
    for (int i : order) {
      vertex(v[i * 3], v[i * 3 + 1], v[i * 3 + 2], nx, ny, nz, mat);
    }
  }

  private void vertex(float x, float y, float z, float nx, float ny, float nz, float mat) {
    if (len + 7 > data.length) data = Arrays.copyOf(data, data.length * 2);
    data[len++] = x;
    data[len++] = y;
    data[len++] = z;
    data[len++] = nx;
    data[len++] = ny;
    data[len++] = nz;
    data[len++] = mat;
  }
}
