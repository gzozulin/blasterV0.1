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

abstract class LwjglWindow(private val width: Int, private val height: Int) {
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

    private val currentPos = Vector2f()
    private val lastCursorPos = Vector2f()
    private val cursorCallback = object : GLFWCursorPosCallback() {
        override fun invoke(window: kotlin.Long, xpos: kotlin.Double, ypos: kotlin.Double) {
            currentPos.set(xpos.toFloat(), ypos.toFloat())
            onCursorPos(currentPos)
            currentPos.sub(lastCursorPos, lastCursorPos)
            onCursorDelta(lastCursorPos)
            lastCursorPos.set(currentPos)
        }
    }

    private fun updateCursorPosition(window: Long) {
        val xbuff = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
        val ybuff = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
        glfwGetCursorPos(window, xbuff, ybuff)
        currentPos.x = xbuff.asDoubleBuffer().get().toFloat()
        currentPos.y = ybuff.asDoubleBuffer().get().toFloat()
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
        updateCursorPosition(window)
        glfwSetKeyCallback(window, keyCallback)
        glfwSetCursorPosCallback(window, cursorCallback)
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

    open fun onCursorPos(position: Vector2f) {}
    open fun onCursorDelta(delta: Vector2f) {}
}