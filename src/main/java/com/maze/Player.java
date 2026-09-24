package com.maze;

/**
 * Jogador: posição, direção, movimento suavizado e colisão círculo-vs-tiles com deslizamento
 * nas paredes. Não depende de OpenGL (testável isoladamente).
 */
public final class Player {

  public static final float RADIUS = 0.32f;
  public static final float EYE_HEIGHT = 1.3f;
  public static final float WALK_SPEED = 3.4f;
  public static final float SPRINT_SPEED = 5.6f;
  public static final float TURN_SPEED = 2.3f;   // rad/s (setas / Q / E)
  private static final float ACCEL = 14f;         // suavização da velocidade
  private static final float MOUSE_SENS = 0.0022f;
  private static final float MAX_PITCH = (float) Math.toRadians(65);

  public float x, z;
  public float yaw;    // 0 = +x, PI/2 = +z
  public float pitch;

  private float vx, vz;
  private float bobPhase, bobAmount;

  private Player() {}

  /** Cria o jogador no centro do tile de entrada, olhando para dentro do labirinto. */
  public static Player atEntrance(Maze maze) {
    Player p = new Player();
    p.x = maze.centerX(maze.startX);
    p.z = maze.centerZ(maze.startY);
    p.yaw = (float) (Math.PI / 2);
    return p;
  }

  public int tileX() {
    return (int) Math.floor(x / Maze.TILE);
  }

  public int tileZ() {
    return (int) Math.floor(z / Maze.TILE);
  }

  public float eyeY() {
    return EYE_HEIGHT + (float) Math.sin(bobPhase) * 0.035f * bobAmount;
  }

  public float dirX() {
    return (float) (Math.cos(pitch) * Math.cos(yaw));
  }

  public float dirY() {
    return (float) Math.sin(pitch);
  }

  public float dirZ() {
    return (float) (Math.cos(pitch) * Math.sin(yaw));
  }

  /** Movimento do mouse (em pixels). */
  public void look(float dx, float dy) {
    yaw += dx * MOUSE_SENS;
    pitch -= dy * MOUSE_SENS;
    pitch = Math.max(-MAX_PITCH, Math.min(MAX_PITCH, pitch));
  }

  /**
   * @param forward  -1..1 (frente/trás)
   * @param strafe   -1..1 (esquerda/direita)
   * @param turn     -1..1 (girar esquerda/direita)
   */
  public void update(float dt, float forward, float strafe, float turn, boolean sprint, Maze maze) {
    yaw += turn * TURN_SPEED * dt;

    float fx = (float) Math.cos(yaw), fz = (float) Math.sin(yaw);
    float rx = -fz, rz = fx;
    float mx = fx * forward + rx * strafe;
    float mz = fz * forward + rz * strafe;
    float len = (float) Math.sqrt(mx * mx + mz * mz);
    if (len > 1f) {
      mx /= len;
      mz /= len;
    }

    float speed = sprint ? SPRINT_SPEED : WALK_SPEED;
    float k = Math.min(1f, ACCEL * dt);
    vx += (mx * speed - vx) * k;
    vz += (mz * speed - vz) * k;

    float px = x, pz = z;
    x += vx * dt;
    z += vz * dt;
    collide(maze);

    float moved = dt > 0 ? (float) Math.hypot(x - px, z - pz) / dt : 0f;
    bobPhase += moved * dt * 2.6f;
    float target = Math.min(1.4f, moved / WALK_SPEED);
    bobAmount += (target - bobAmount) * Math.min(1f, 8f * dt);
  }

  /** Empurra o círculo do jogador para fora de qualquer tile de parede sobreposto. */
  private void collide(Maze maze) {
    final float t = Maze.TILE;
    for (int iter = 0; iter < 4; iter++) {
      boolean hit = false;
      int x0 = (int) Math.floor((x - RADIUS) / t), x1 = (int) Math.floor((x + RADIUS) / t);
      int z0 = (int) Math.floor((z - RADIUS) / t), z1 = (int) Math.floor((z + RADIUS) / t);
      for (int ty = z0; ty <= z1; ty++) {
        for (int tx = x0; tx <= x1; tx++) {
          if (!maze.isWall(tx, ty)) continue;
          float minX = tx * t, maxX = minX + t, minZ = ty * t, maxZ = minZ + t;
          float cx = Math.max(minX, Math.min(x, maxX));
          float cz = Math.max(minZ, Math.min(z, maxZ));
          float dx = x - cx, dz = z - cz;
          float d2 = dx * dx + dz * dz;
          if (d2 >= RADIUS * RADIUS) continue;
          hit = true;
          if (d2 > 1e-10f) {
            float d = (float) Math.sqrt(d2);
            float push = (RADIUS - d) / d;
            x += dx * push;
            z += dz * push;
          } else { // centro dentro da parede: empurra pela face mais próxima
            float l = x - minX, r = maxX - x, tp = z - minZ, b = maxZ - z;
            float m = Math.min(Math.min(l, r), Math.min(tp, b));
            if (m == l) x = minX - RADIUS;
            else if (m == r) x = maxX + RADIUS;
            else if (m == tp) z = minZ - RADIUS;
            else z = maxZ + RADIUS;
          }
        }
      }
      if (!hit) break;
    }
  }
}
