package com.maze;

import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

/** Programa GLSL (vertex + fragment) com cache de uniforms. */
final class Shader implements AutoCloseable {

  private final int program;
  private final Map<String, Integer> uniforms = new HashMap<>();
  private final float[] m4 = new float[16];

  Shader(String vertexSrc, String fragmentSrc) {
    int vs = compile(GL_VERTEX_SHADER, vertexSrc);
    int fs = compile(GL_FRAGMENT_SHADER, fragmentSrc);
    program = glCreateProgram();
    glAttachShader(program, vs);
    glAttachShader(program, fs);
    glLinkProgram(program);
    if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
      throw new IllegalStateException("Falha ao linkar shader: " + glGetProgramInfoLog(program));
    }
    glDeleteShader(vs);
    glDeleteShader(fs);
  }

  private static int compile(int type, String src) {
    int id = glCreateShader(type);
    glShaderSource(id, src);
    glCompileShader(id);
    if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new IllegalStateException("Falha ao compilar shader: " + glGetShaderInfoLog(id));
    }
    return id;
  }

  void use() {
    glUseProgram(program);
  }

  private int loc(String name) {
    return uniforms.computeIfAbsent(name, n -> glGetUniformLocation(program, n));
  }

  void setMat4(String name, Matrix4f m) {
    m.get(m4);
    glUniformMatrix4fv(loc(name), false, m4);
  }

  void setVec3(String name, float x, float y, float z) {
    glUniform3f(loc(name), x, y, z);
  }

  void setVec2(String name, float x, float y) {
    glUniform2f(loc(name), x, y);
  }

  void setInt(String name, int v) {
    glUniform1i(loc(name), v);
  }

  void setFloat(String name, float v) {
    glUniform1f(loc(name), v);
  }

  @Override
  public void close() {
    glDeleteProgram(program);
  }
}
