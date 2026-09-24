package com.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Menu vertical de botões, navegável por teclado e mouse. */
final class Menu {

  private static final float ITEM_W = 560f, ITEM_H = 64f, GAP = 18f, PS = 5f;

  private static final class Item {
    final Supplier<String> label;
    final Runnable action;
    final IntConsumer adjust; // opcional (setas esquerda/direita)

    Item(Supplier<String> label, Runnable action, IntConsumer adjust) {
      this.label = label;
      this.action = action;
      this.adjust = adjust;
    }
  }

  private final List<Item> items = new ArrayList<>();
  private final float top;
  int selected;

  Menu(float top) {
    this.top = top;
  }

  Menu add(Supplier<String> label, Runnable action) {
    items.add(new Item(label, action, null));
    return this;
  }

  Menu addAdjustable(Supplier<String> label, Runnable action, IntConsumer adjust) {
    items.add(new Item(label, action, adjust));
    return this;
  }

  void move(int dir) {
    selected = (selected + dir + items.size()) % items.size();
  }

  void activate() {
    items.get(selected).action.run();
  }

  void adjust(int dir) {
    IntConsumer a = items.get(selected).adjust;
    if (a != null) a.accept(dir);
  }

  /** Índice do botão sob (x, y) em coordenadas virtuais, ou -1. */
  int hit(float x, float y, float vw) {
    float left = (vw - ITEM_W) / 2f;
    if (x < left || x > left + ITEM_W) return -1;
    for (int i = 0; i < items.size(); i++) {
      float iy = top + i * (ITEM_H + GAP);
      if (y >= iy && y <= iy + ITEM_H) return i;
    }
    return -1;
  }

  void draw(Hud hud, float vw) {
    float x = (vw - ITEM_W) / 2f;
    for (int i = 0; i < items.size(); i++) {
      float y = top + i * (ITEM_H + GAP);
      String label = items.get(i).label.get();
      float ty = y + (ITEM_H - 7 * PS) / 2f;
      if (i == selected) {
        hud.rect(x, y, ITEM_W, ITEM_H, 1f, 1f);
        hud.textCentered(label, vw / 2f, ty, PS, 0f, 1f);
      } else {
        hud.rect(x, y, ITEM_W, ITEM_H, 0.04f, 0.55f);
        hud.frame(x, y, ITEM_W, ITEM_H, 3f, 0.55f, 1f);
        hud.textCentered(label, vw / 2f, ty, PS, 0.85f, 1f);
      }
    }
  }
}
