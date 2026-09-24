package com.maze;

import java.text.Normalizer;
import java.util.Locale;

/** Tamanhos de labirinto disponíveis (número de células por lado). */
public enum MazeSize {
  PEQUENO("PEQUENO", 8),
  MEDIO("MÉDIO", 14),
  GRANDE("GRANDE", 22);

  public final String label;
  public final int cells;

  MazeSize(String label, int cells) {
    this.label = label;
    this.cells = cells;
  }

  public static MazeSize parse(String text) {
    String t = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toUpperCase(Locale.ROOT);
    for (MazeSize s : values()) {
      if (s.name().equals(t)) return s;
    }
    throw new IllegalArgumentException("Tamanho inválido: " + text + " (use pequeno, medio ou grande)");
  }
}
