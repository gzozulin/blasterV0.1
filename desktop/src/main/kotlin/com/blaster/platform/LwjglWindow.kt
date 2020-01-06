package com.blaster.platform

import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL

abstract class LwjglWindow(private val width: Int, private val height: Int) {
    init {
        SharedLibraryLoader.load()
    }

    private var errorCallback: GLFWErrorCallback = errorCallbackPrint(System.err)
    private var keyCallback: GLFWKeyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (action == GLFW_RELEASE) {
                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, GL11.GL_TRUE)
                }
                keyReleased(key)
            } else if (action == GLFW_PRESS) {
                keyPressed(key)
            }
        }
    }

    private var fps = 0
    private var last = System.currentTimeMillis()

    fun show() {
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_TRUE)
        val window = glfwCreateWindow(width, height, "Blaster!", NULL, NULL)
        glfwSetKeyCallback(window, keyCallback)
        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(window, (GLFWvidmode.width(videoMode) - width) / 2, (GLFWvidmode.height(videoMode) - height) / 2)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
        GLContext.createFromCurrent()
        onCreate()
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            onDraw()
            glfwSwapBuffers(window)
            glfwPollEvents()
            fps++
            val current = System.currentTimeMillis()
            if (current - last > 1000L) {
                glfwSetWindowTitle(window, "Blaster! $fps fps")
                last = current
                fps = 0
            }
        }
        glfwDestroyWindow(window)
        keyCallback.release()
    }

    protected abstract fun onCreate()
    protected abstract fun onDraw()

    open fun keyPressed(key: Int) {}
    open fun keyReleased(key: Int) {}
}