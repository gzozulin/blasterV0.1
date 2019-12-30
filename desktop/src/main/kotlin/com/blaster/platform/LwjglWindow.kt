package com.blaster.platform

import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL

const val WIDTH = 800
const val HEIGHT = 600

abstract class LwjglWindow {
    init {
        SharedLibraryLoader.load()
    }

    private var errorCallback: GLFWErrorCallback = errorCallbackPrint(System.err)
    private var keyCallback: GLFWKeyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, GL11.GL_TRUE)
            }
        }
    }

    fun show() {
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_TRUE)
        val window = glfwCreateWindow(WIDTH, HEIGHT, "Blaster!", NULL, NULL)
        glfwSetKeyCallback(window, keyCallback)
        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(window, (GLFWvidmode.width(videoMode) - WIDTH) / 2, (GLFWvidmode.height(videoMode) - HEIGHT) / 2)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
        GLContext.createFromCurrent()
        onCreate(WIDTH, HEIGHT)
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            onDraw()
            glfwSwapBuffers(window)
            glfwPollEvents()
        }
        glfwDestroyWindow(window)
        keyCallback.release()
    }

    protected abstract fun onCreate(width: Int, height: Int)
    protected abstract fun onDraw()
}