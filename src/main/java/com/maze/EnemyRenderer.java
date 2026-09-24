package com.maze;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL33C.*;

/**
 * Desenha o inimigo como uma imagem estática (billboard) que sempre olha para a câmera.
 * A imagem vem de {@code src/main/resources/enemy.png}. Se ela não existir, usa um rosto
 * procedural (quadrado escuro de olhos brilhantes) como reserva.
 */
final class EnemyRenderer implements AutoCloseable {

  private static final float HEIGHT = 1.2f;   // altura do quadro; a largura segue a proporção da imagem
  private static final String TEXTURE = "/enemy.png";

  private static final String VERTEX = """
      #version 330 core
      uniform mat4 uViewProj;
      uniform vec3 uCenter;
      uniform vec3 uRight;
      uniform vec2 uSize;
      out vec2 vUV;
      void main() {
        vec2 c = vec2(float(gl_VertexID & 1), float((gl_VertexID >> 1) & 1));
        vec3 pos = uCenter + uRight * (c.x - 0.5) * uSize.x + vec3(0.0, 1.0, 0.0) * (c.y - 0.5) * uSize.y;
        vUV = c;
        gl_Position = uViewProj * vec4(pos, 1.0);
      }
      """;

  private static final String FRAGMENT = """
      #version 330 core
      in vec2 vUV;
      uniform float uTime;
      uniform float uDist;
      uniform sampler2D uTex;
      uniform float uHasTex;
      out vec4 FragColor;
      void main() {
        vec2 p = vUV;
        float fog = exp(-uDist * 0.06);

        if (uHasTex > 0.5) {
          vec3 c = texture(uTex, vec2(p.x, 1.0 - p.y)).rgb;
          float light = 0.4 + 0.6 * fog;   // mais escuro ao longe, mas nunca invisível
          FragColor = vec4(c * light, 1.0);
          return;
        }

        float border = min(step(p.x, 0.04) + step(0.96, p.x) + step(p.y, 0.04) + step(0.96, p.y), 1.0);
        float body = 0.05 + 0.03 * sin(uTime * 2.0 + p.y * 8.0);
        body = mix(body, 0.5, border);

        vec2 e1 = (p - vec2(0.3, 0.62)) / vec2(0.11, 0.075);
        vec2 e2 = (p - vec2(0.7, 0.62)) / vec2(0.11, 0.075);
        float eyes = 1.0 - smoothstep(0.7, 1.0, min(dot(e1, e1), dot(e2, e2)));
        eyes *= 0.85 + 0.15 * sin(uTime * 6.0);

        float zig = abs(fract(p.x * 6.0) - 0.5) * 2.0;
        float mouth = step(abs(p.y - (0.30 + 0.07 * zig)), 0.028) * step(0.2, p.x) * step(p.x, 0.8);

        // corpo some na névoa; olhos e boca continuam visíveis de longe
        float glow = max(eyes, mouth * 0.9);
        float g = max(body * fog, glow * mix(fog, 1.0, 0.55));
        FragColor = vec4(vec3(g), 1.0);
      }
      """;

  private final Shader shader = new Shader(VERTEX, FRAGMENT);
  private final int vao = glGenVertexArrays(); // VAO vazio: os vértices vêm de gl_VertexID
  private final Texture texture = Texture.loadResource(TEXTURE);
  private final float width = texture != null ? HEIGHT * texture.width / texture.height : HEIGHT;

  void render(Enemy e, Player p, Matrix4f viewProj, float time) {
    float ex = e.x, ez = e.z;
    float ey = 0.95f + (float) Math.sin(time * 2.0) * 0.06f;

    float cx = p.x, cz = p.z;
    float dx = ex - cx, dz = ez - cz;
    float len = (float) Math.hypot(dx, dz);
    if (len < 1e-4f) return;
    float fx = dx / len, fz = dz / len;
    float rx = -fz, rz = fx; // direita da câmera (billboard vertical)

    float dy = ey - p.eyeY();
    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

    glEnable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    glDisable(GL_BLEND);

    shader.use();
    shader.setMat4("uViewProj", viewProj);
    shader.setVec3("uCenter", ex, ey, ez);
    shader.setVec3("uRight", rx, 0f, rz);
    shader.setVec2("uSize", width, HEIGHT);
    if (texture != null) {
      texture.bind(0);
      shader.setInt("uTex", 0);
    }
    shader.setFloat("uHasTex", texture != null ? 1f : 0f);
    shader.setFloat("uTime", time);
    shader.setFloat("uDist", dist);
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glBindVertexArray(0);
  }

  @Override
  public void close() {
    glDeleteVertexArrays(vao);
    if (texture != null) texture.close();
    shader.close();
  }
}
