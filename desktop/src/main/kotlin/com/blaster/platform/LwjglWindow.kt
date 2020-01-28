package com.blaster.platform

import org.joml.Vector2f
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

// todo: hold cursor only if focused
abstract class LwjglWindow(
        private val width: Int = 1024, private val height: Int = 768,
        private val fullWidth: Int = 1920, private val fullHeight: Int = 1080,
        private val isHoldingCursor: Boolean = true) {

    init {
        SharedLibraryLoader.load()
    }

    private var window = 0L

    private var isFullscreen = false

    private val screenWidth: Int
        get() = if (isFullscreen) fullWidth else width
    private val screenHeight: Int
        get() = if (isFullscreen) fullHeight else height

    private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val xbufDouble = xbuf.asDoubleBuffer()
    private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val ybufDouble = ybuf.asDoubleBuffer()
    private val currentPos = Vector2f()
    private val lastCursorPos = Vector2f()

    private var fps = 0
    private var last = System.currentTimeMillis()

    private val errorCallback = errorCallbackPrint(System.err)

    private val keyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (action == GLFW_RELEASE) {
                when (key) {
                    GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, GL11.GL_TRUE)
                    GLFW_KEY_F12 -> switchFullscreen()
                    else -> keyReleased(key)
                }
            } else if (action == GLFW_PRESS) {
                keyPressed(key)
            }
        }
    }

    private val mouseBtnCallback = object : GLFWMouseButtonCallback() {
        override fun invoke(window: kotlin.Long, button: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (action == GLFW_RELEASE) {
                mouseBtnReleased(button)
            } else if (action == GLFW_PRESS) {
                mouseBtnPressed(button)
            }
        }
    }

    private fun switchFullscreen() {
        isFullscreen = !isFullscreen
        createWindow()
    }

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

    fun show() {
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        createWindow()
        onCreate(screenWidth, screenHeight)
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            updateCursor(window)
            onTick()
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

    private fun createWindow() {
        val new = if (isFullscreen) {
            glfwCreateWindow(fullWidth, fullHeight, "Blaster!", glfwGetPrimaryMonitor(), window)
        } else {
            val wnd = glfwCreateWindow(width, height, "Blaster!", NULL, window)
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            glfwSetWindowPos(wnd, (GLFWvidmode.width(videoMode) - width) / 2, (GLFWvidmode.height(videoMode) - height) / 2)
            wnd
        }
        if (isHoldingCursor) {
            glfwSetInputMode(new, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        glfwSetMouseButtonCallback(new, mouseBtnCallback)
        glfwSetKeyCallback(new, keyCallback)
        glfwMakeContextCurrent(new)
        glfwSwapInterval(1)
        GLContext.createFromCurrent()
        if (window > NULL) {
            glfwDestroyWindow(window)
        }
        window = new
        glfwShowWindow(new)
    }

    protected abstract fun onCreate(width: Int, height: Int)
    protected abstract fun onTick()

    open fun mouseBtnPressed(btn: Int) {}
    open fun mouseBtnReleased(btn: Int) {}

    open fun keyPressed(key: Int) {}
    open fun keyReleased(key: Int) {}

    open fun onCursorPos(position: Vector2f) {}
    open fun onCursorDelta(delta: Vector2f) {}
}