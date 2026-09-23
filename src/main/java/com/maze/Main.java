package com.maze;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

  public static void main(String[] args) {

    System.out.println("Starting 3D Maze...");

    GLFWErrorCallback.createPrint(System.err).set();

    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    long window = glfwCreateWindow(
        1280,
        720,
        "3D Maze",
        NULL,
        NULL
    );

    if (window == NULL) {
      throw new RuntimeException("Failed to create GLFW window");
    }

    glfwMakeContextCurrent(window);

    GL.createCapabilities();

    System.out.println("OpenGL initialized!");

    while (!glfwWindowShouldClose(window)) {

      glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT);

      glfwSwapBuffers(window);
      glfwPollEvents();
    }

    glfwDestroyWindow(window);
    glfwTerminate();

    System.out.println("Game closed.");
  }
}