package com.maze;

/**
 * Ponto de entrada.
 *
 * Opções:
 *   --seed N            usa sempre a mesma seed (mesmo labirinto a cada partida)
 *   --size pequeno|medio|grande
 */
public class Main {

  public static void main(String[] args) {
    Long seed = null;
    MazeSize size = MazeSize.MEDIO;

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--seed" -> seed = Long.parseLong(args[++i]);
        case "--size" -> size = MazeSize.parse(args[++i]);
        default -> System.err.println("Argumento ignorado: " + args[i]);
      }
    }

    System.out.println("Starting 3D Maze...");
    new Game(seed, size).run();
  }
}
