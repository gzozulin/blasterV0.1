package com.blaster.platform

import org.joml.Vector2f
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

// todo: swap fullscreen button
// http://www.java-gaming.org/topics/glfw-lwjgl3-toggle-between-fullscreen-and-windowed-mode/34882/view.html
abstract class LwjglWindow(
        private val width: Int = 800, private val height: Int = 600,
        private val fullscreen: Boolean = false) {

    init {
        SharedLibraryLoader.load()
    }

    private val errorCallback = errorCallbackPrint(System.err)

    private val keyCallback = object : GLFWKeyCallback() {
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

    private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val xbufDouble = xbuf.asDoubleBuffer()
    private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val ybufDouble = ybuf.asDoubleBuffer()
    private val currentPos = Vector2f()
    private val lastCursorPos = Vector2f()
    private fun updateCursor(window: Long) {
        xbuf.rewind()
        xbufDouble.rewind()
        ybuf.rewind()
        ybufDouble.rewind()
        glfwGetCursorPos(window, xbuf, ybuf)
        currentPos.set(xbufDouble.get().toFloat(), ybufDouble.get().toFloat())
        if (lastCursorPos.x == 0f && lastCursorPos.y == 0f) {
            lastCursorPos.set(currentPos.x, currentPos.y)
        }
        onCursorPos(currentPos)
        currentPos.sub(lastCursorPos, lastCursorPos)
        onCursorDelta(lastCursorPos)
        lastCursorPos.set(currentPos)
    }

    private var fps = 0
    private var last = System.currentTimeMillis()

    fun show() {
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_TRUE)
        val window = glfwCreateWindow(width, height, "Blaster!", if (fullscreen) glfwGetPrimaryMonitor() else NULL, NULL)
        if (!fullscreen) {
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            glfwSetWindowPos(window, (GLFWvidmode.width(videoMode) - width) / 2, (GLFWvidmode.height(videoMode) - height) / 2)
        }
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        glfwSetKeyCallback(window, keyCallback)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
        GLContext.createFromCurrent()
        onCreate()
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            updateCursor(window)
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

    open fun onCursorPos(position: Vector2f) {}
    open fun onCursorDelta(delta: Vector2f) {}
}