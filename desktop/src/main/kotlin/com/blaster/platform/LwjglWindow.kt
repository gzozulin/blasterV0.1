package com.blaster.platform

import com.blaster.auxiliary.Once
import com.blaster.auxiliary.vec2
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val winWidth: Int = 1024
private const val winHeight: Int = 768

private const val fullWidth: Int = 1920
private const val fullHeight: Int = 1080

private const val winX: Int = 448
private const val winY: Int = 156

abstract class LwjglWindow(
        private val isHoldingCursor: Boolean = true,
        private var isFullscreen: Boolean = false,
        private val isMultisampled: Boolean = false) {

    init {
        loadSharedLibs()
    }

    private var window = NULL
    private val contextCreated = Once()

    private val xbuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val xbufDouble = xbuf.asDoubleBuffer()
    private val ybuf = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder())
    private val ybufDouble = ybuf.asDoubleBuffer()
    private val currentPos = vec2()
    private val lastCursorPos = vec2()

    private var fps = 0
    private var last = System.currentTimeMillis()

    private val errorCallback = errorCallbackPrint(System.err)

    private fun loadSharedLibs() {
        SharedLibraryLoader.load()
    }

    private val windowSizeCallback = object : GLFWWindowSizeCallback() {
        override fun invoke(window: kotlin.Long, width: kotlin.Int, height: kotlin.Int) {
            onResize(width, height)
        }
    }

    private val keyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (action == GLFW_RELEASE) {
                when (key) {
                    GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, GL11.GL_TRUE)
                    GLFW_KEY_F -> switchFullscreen()
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
        val oldWindow = window
        window = createWindow()
        destroyWindow(oldWindow)
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

    // This method allows us to count fps and update it in the title of the window
    private fun updateFps() {
        val current = System.currentTimeMillis()
        // If more then a second passed - print and zero the counter
        if (current - last > 1000L) {
            glfwSetWindowTitle(window, "Blaster! $fps fps")
            last = current
            fps = 0
            // else - increment it
        } else {
            fps++
        }
    }

    fun show() {
        glfwSetErrorCallback(errorCallback)
        check(glfwInit() == GL11.GL_TRUE)
        window = createWindow()
        while (glfwWindowShouldClose(window) == GL11.GL_FALSE) {
            updateCursor(window)
            onTick()
            glfwSwapBuffers(window)
            glfwPollEvents()
            updateFps()
        }
        destroyWindow(window)
        keyCallback.release()
    }

    private fun createWindow(): Long {
        val result = if (isFullscreen) {
            glfwCreateWindow(fullWidth, fullHeight, "Blaster!", glfwGetPrimaryMonitor(), window)
        } else {
            glfwCreateWindow(winWidth, winHeight, "Blaster!", NULL, window)
        }
        if (!isFullscreen) {
            glfwSetWindowPos(result, winX, winY)
        }
        if (isHoldingCursor) {
            glfwSetInputMode(result, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
        if (isMultisampled) {
            glfwWindowHint(GLFW_SAMPLES, 4)
        }
        glfwSetWindowSizeCallback(result, windowSizeCallback)
        glfwSetMouseButtonCallback(result, mouseBtnCallback)
        glfwSetKeyCallback(result, keyCallback)
        glfwMakeContextCurrent(result)
        glfwSwapInterval(1)
        GLContext.createFromCurrent()
        glfwShowWindow(result)
        if (contextCreated.check()) {
            onCreate()
        }
        onResize(if (isFullscreen) fullWidth else winWidth,
                if (isFullscreen) fullHeight else winHeight)
        return result
    }

    private fun destroyWindow(handle: Long) {
        check(handle > NULL) { "Invalid handle!" }
        glfwDestroyWindow(handle)
    }

    protected abstract fun onCreate()
    protected open fun onResize(width: Int, height: Int) {}
    protected abstract fun onTick()

    open fun mouseBtnPressed(btn: Int) {}
    open fun mouseBtnReleased(btn: Int) {}

    open fun keyPressed(key: Int) {}
    open fun keyReleased(key: Int) {}

    open fun onCursorPos(position: vec2) {}
    open fun onCursorDelta(delta: vec2) {}
}