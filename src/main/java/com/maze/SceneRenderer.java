package com.maze;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL33C.*;

/**
 * Renderiza o labirinto em 3D. Visual monocromático: os "materiais" (tijolo, piso, teto, saída)
 * são procedurais no fragment shader, iluminados por uma lanterna que acompanha o jogador,
 * uma luz fraca na saída e névoa para dar profundidade.
 */
final class SceneRenderer implements AutoCloseable {

  private static final float FOV_DEGREES = 75f;

  private static final String VERTEX = """
      #version 330 core
      layout(location = 0) in vec3 aPos;
      layout(location = 1) in vec3 aNormal;
      layout(location = 2) in float aMat;
      uniform mat4 uViewProj;
      out vec3 vPos;
      out vec3 vNormal;
      flat out float vMat;
      void main() {
        vPos = aPos;
        vNormal = aNormal;
        vMat = aMat;
        gl_Position = uViewProj * vec4(aPos, 1.0);
      }
      """;

  private static final String FRAGMENT = """
      #version 330 core
      in vec3 vPos;
      in vec3 vNormal;
      flat in float vMat;
      uniform vec3 uCamPos;
      uniform vec3 uExitPos;
      uniform float uTime;
      out vec4 FragColor;

      float hash(vec2 p) {
        p = fract(p * vec2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
      }

      float vnoise(vec2 p) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        float a = hash(i);
        float b = hash(i + vec2(1.0, 0.0));
        float c = hash(i + vec2(0.0, 1.0));
        float d = hash(i + vec2(1.0, 1.0));
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
      }

      void main() {
        vec3 n = normalize(vNormal);
        int mat = int(vMat + 0.5);
        vec3 an = abs(n);
        vec2 uv = an.y > 0.5 ? vPos.xz : (an.x > 0.5 ? vPos.zy : vPos.xy);

        float albedo;
        float emissive = 0.0;

        if (mat == 0) {                       // parede: tijolos
          float rowH = 2.5;
          float row = floor(uv.y * rowH);
          float bx = uv.x + mod(row, 2.0) * 0.5;
          vec2 cell = vec2(floor(bx), row);
          vec2 f = vec2(fract(bx), fract(uv.y * rowH));
          float mortar = min(step(f.x, 0.05) + step(f.y, 0.08), 1.0);
          albedo = 0.62 + (hash(cell) - 0.5) * 0.22 + (vnoise(uv * 14.0) - 0.5) * 0.12;
          albedo = mix(albedo, 0.16, mortar);
        } else if (mat == 1) {                // chão: lajotas
          vec2 f = fract(uv);
          float line = min(step(f.x, 0.03) + step(f.y, 0.03), 1.0);
          albedo = 0.42 + (hash(floor(uv)) - 0.5) * 0.10 + (vnoise(uv * 10.0) - 0.5) * 0.08;
          albedo = mix(albedo, 0.12, line);
        } else if (mat == 2) {                // teto: painéis escuros
          vec2 f = fract(uv * 0.5);
          float line = min(step(f.x, 0.02) + step(f.y, 0.02), 1.0);
          albedo = 0.26 + (vnoise(uv * 6.0) - 0.5) * 0.06;
          albedo = mix(albedo, 0.08, line);
        } else {                              // saída: emissiva, pulsa devagar
          albedo = 1.0;
          emissive = 0.88 + 0.12 * sin(uTime * 3.0);
        }

        // Lanterna do jogador
        vec3 toCam = uCamPos - vPos;
        float d = length(toCam);
        vec3 L = toCam / max(d, 0.0001);
        float diff = max(dot(n, L), 0.0);
        float att = 1.0 / (1.0 + 0.09 * d + 0.07 * d * d);
        float light = 0.05 + diff * att * 1.8;

        // Luz da saída (curto alcance, sem sombras)
        vec3 toE = uExitPos - vPos;
        float de = length(toE);
        float diffE = max(dot(n, toE / max(de, 0.0001)), 0.0);
        light += diffE * 1.3 / (1.0 + 0.15 * de * de);

        float fog = exp(-d * 0.06);
        float g = albedo * light * fog;

        if (mat == 3) {
          float fogE = mix(fog, 1.0, 0.65);   // a saída continua visível de longe
          g = emissive * fogE;
        }

        g = pow(clamp(g, 0.0, 1.0), 1.0 / 2.2);
        FragColor = vec4(vec3(g), 1.0);
      }
      """;

  private final Shader shader = new Shader(VERTEX, FRAGMENT);
  private final Matrix4f proj = new Matrix4f();
  private final Matrix4f view = new Matrix4f();
  private final Matrix4f viewProj = new Matrix4f();

  private Mesh mesh;
  private float exitX, exitY, exitZ;

  void setMaze(Maze maze) {
    if (mesh != null) mesh.close();
    mesh = MazeMeshBuilder.build(maze);
    exitX = maze.centerX(maze.exitX);
    exitY = Player.EYE_HEIGHT;
    exitZ = maze.centerZ(maze.exitY);
  }

  int vertexCount() {
    return mesh == null ? 0 : mesh.vertexCount();
  }

  void render(Player p, float aspect, float time) {
    if (mesh == null) return;

    float ex = p.x, ey = p.eyeY(), ez = p.z;
    proj.setPerspective((float) Math.toRadians(FOV_DEGREES), aspect, 0.05f, 150f);
    view.setLookAt(ex, ey, ez, ex + p.dirX(), ey + p.dirY(), ez + p.dirZ(), 0f, 1f, 0f);
    proj.mul(view, viewProj);

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glDisable(GL_BLEND);

    shader.use();
    shader.setMat4("uViewProj", viewProj);
    shader.setVec3("uCamPos", ex, ey, ez);
    shader.setVec3("uExitPos", exitX, exitY, exitZ);
    shader.setFloat("uTime", time);
    mesh.draw();
  }

  @Override
  public void close() {
    if (mesh != null) mesh.close();
    shader.close();
  }
}
