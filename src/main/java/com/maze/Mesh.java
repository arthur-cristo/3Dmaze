package com.maze;

import java.util.Arrays;

import static org.lwjgl.opengl.GL33C.*;

/** Malha estática de triângulos (VAO + VBO) com atributos float intercalados. */
final class Mesh implements AutoCloseable {

  private final int vao;
  private final int vbo;
  private final int vertexCount;

  Mesh(float[] data, int length, int... attribSizes) {
    int floatsPerVertex = 0;
    for (int s : attribSizes) floatsPerVertex += s;
    vertexCount = length / floatsPerVertex;

    vao = glGenVertexArrays();
    vbo = glGenBuffers();
    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, Arrays.copyOf(data, length), GL_STATIC_DRAW);

    int stride = floatsPerVertex * Float.BYTES;
    long offset = 0;
    for (int i = 0; i < attribSizes.length; i++) {
      glVertexAttribPointer(i, attribSizes[i], GL_FLOAT, false, stride, offset);
      glEnableVertexAttribArray(i);
      offset += (long) attribSizes[i] * Float.BYTES;
    }
    glBindVertexArray(0);
  }

  int vertexCount() {
    return vertexCount;
  }

  void draw() {
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    glBindVertexArray(0);
  }

  @Override
  public void close() {
    glDeleteBuffers(vbo);
    glDeleteVertexArrays(vao);
  }
}
