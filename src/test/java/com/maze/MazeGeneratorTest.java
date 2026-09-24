package com.maze;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MazeGeneratorTest {

  private static final int[] SIZES = {2, 3, 8, 14, 22, 40};

  @Test
  void everySeedProducesASolvableMaze() {
    for (int cells : SIZES) {
      for (long seed = 0; seed < 500; seed++) {
        Maze m = MazeGenerator.generate(cells, cells, seed);
        assertTrue(m.shortestPath() > 0, "sem caminho: cells=" + cells + " seed=" + seed);
      }
    }
  }

  @Test
  void entranceAndExitAreOpenAndBordersAreClosed() {
    for (int cells : SIZES) {
      for (long seed = 0; seed < 100; seed++) {
        Maze m = MazeGenerator.generate(cells, cells, seed);
        assertFalse(m.isWall(m.startX, m.startY));
        assertFalse(m.isWall(m.exitX, m.exitY));
        for (int y = 0; y < m.height; y++) {
          for (int x = 0; x < m.width; x++) {
            boolean border = x == 0 || y == 0 || x == m.width - 1 || y == m.height - 1;
            boolean allowed = (x == m.startX && y == m.startY) || (x == m.exitX && y == m.exitY);
            if (border && !allowed) assertTrue(m.isWall(x, y), "borda aberta em " + x + "," + y);
          }
        }
      }
    }
  }

  @Test
  void sameSeedGivesSameMazeAndDifferentSeedsDiffer() {
    Maze a = MazeGenerator.generate(14, 14, 123);
    Maze b = MazeGenerator.generate(14, 14, 123);
    Maze c = MazeGenerator.generate(14, 14, 124);
    boolean differs = false;
    for (int y = 0; y < a.height; y++) {
      for (int x = 0; x < a.width; x++) {
        assertEquals(a.isWall(x, y), b.isWall(x, y));
        if (a.isWall(x, y) != c.isWall(x, y)) differs = true;
      }
    }
    assertTrue(differs);
  }

  @Test
  void playerNeverEntersWalls() {
    Random rnd = new Random(7);
    for (int s = 0; s < 20; s++) {
      Maze m = MazeGenerator.generate(14, 14, s);
      Player p = Player.atEntrance(m);
      float f = 0, st = 0, tu = 0;
      boolean sprint = false;
      for (int i = 0; i < 10_000; i++) {
        if (i % 40 == 0) {
          f = rnd.nextInt(3) - 1;
          st = rnd.nextInt(3) - 1;
          tu = rnd.nextInt(3) - 1;
          sprint = rnd.nextBoolean();
        }
        p.update(i % 97 == 0 ? 0.05f : 0.016f, f, st, tu, sprint, m);
        assertFalse(m.isWall(p.tileX(), p.tileZ()), "jogador dentro da parede (seed " + s + ")");
      }
    }
  }
}
