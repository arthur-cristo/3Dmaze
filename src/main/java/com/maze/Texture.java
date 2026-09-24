package com.maze;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33C.*;

/** Textura 2D carregada de um recurso do classpath (PNG/JPG) via ImageIO. */
final class Texture implements AutoCloseable {

  private final int id;
  final int width;
  final int height;

  private Texture(int id, int width, int height) {
    this.id = id;
    this.width = width;
    this.height = height;
  }

  /** Retorna null se o recurso não existir ou não puder ser lido. */
  static Texture loadResource(String path) {
    try (InputStream in = Texture.class.getResourceAsStream(path)) {
      if (in == null) return null;
      BufferedImage img = ImageIO.read(in);
      if (img == null) return null;

      int w = img.getWidth(), h = img.getHeight();
      int[] argb = img.getRGB(0, 0, w, h, null, 0, w);
      ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
      for (int p : argb) {
        buf.put((byte) ((p >> 16) & 0xFF));
        buf.put((byte) ((p >> 8) & 0xFF));
        buf.put((byte) (p & 0xFF));
        buf.put((byte) ((p >>> 24) & 0xFF));
      }
      buf.flip();

      int id = glGenTextures();
      glBindTexture(GL_TEXTURE_2D, id);
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
      glGenerateMipmap(GL_TEXTURE_2D);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      return new Texture(id, w, h);
    } catch (IOException | RuntimeException e) {
      System.err.println("Não foi possível carregar " + path + ": " + e);
      return null;
    }
  }

  void bind(int unit) {
    glActiveTexture(GL_TEXTURE0 + unit);
    glBindTexture(GL_TEXTURE_2D, id);
  }

  @Override
  public void close() {
    glDeleteTextures(id);
  }
}
