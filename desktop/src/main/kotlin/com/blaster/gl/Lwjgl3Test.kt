package com.blaster.gl

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.renderers.SimpleRenderer
import com.example.desktop.SharedLibraryLoader
import org.lwjgl.Sys
import org.lwjgl.glfw.Callbacks.errorCallbackPrint
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWvidmode
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLContext
import org.lwjgl.system.MemoryUtil.NULL
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.nio.ByteBuffer

const val WIDTH = 800
const val HEIGHT = 600

private val assetStream = object : AssetStream {
    override fun openAsset(filename: String): InputStream {
        val assets = File("desktop/assets")
        val resource = File(assets, filename)
        return try {
            resource.inputStream()
        } catch (e: Exception) {
            ByteArrayInputStream("It doesnt matter for now".toByteArray(Charsets.UTF_8))
        }
    }
}

private val pixelDecoder = object : PixelDecoder {
    override fun decodePixels(inputStream: InputStream): PixelDecoder.Decoded {
        val buffer = ByteBuffer.allocateDirect(4 * 4)
        buffer.asIntBuffer().put(intArrayOf(1, 2, 3, 4))
        return PixelDecoder.Decoded(buffer, 2, 2)
    }
}

private val shadersLib = ShadersLib(assetStream)
private val textureLib = TexturesLib(assetStream, pixelDecoder)
private val simpleRenderer = SimpleRenderer(shadersLib, textureLib)

class Lwjgl3Test {
    private var errorCallback: GLFWErrorCallback = errorCallbackPrint(System.err)
    private var keyCallback: GLFWKeyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: kotlin.Long, key: kotlin.Int, scancode: kotlin.Int, action: kotlin.Int, mods: kotlin.Int) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, GL_TRUE) // We will detect this in our rendering loop
        }
    }
    private var window: Long = 0

    fun run() {
        println("Hello LWJGL " + Sys.getVersion() + "!")
        try {
            init()
            loop()
            glfwDestroyWindow(window)
            keyCallback.release()
        } finally {
            glfwTerminate()
            errorCallback.release()
        }
    }

    private fun init() {
        glfwSetErrorCallback(errorCallback)
        if (glfwInit() != GL11.GL_TRUE)
            throw IllegalStateException("Unable to initialize GLFW")
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE)
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL)
        glfwSetKeyCallback(window, keyCallback)
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - WIDTH) / 2, (GLFWvidmode.height(vidmode) - HEIGHT) / 2)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
    }

    private fun loop() {
        GLContext.createFromCurrent()

        simpleRenderer.onCreate()
        simpleRenderer.onChange(WIDTH, HEIGHT)


        while (glfwWindowShouldClose(window) == GL_FALSE) {
            //glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            simpleRenderer.onDraw()

            glfwSwapBuffers(window) // swap the color buffers
            glfwPollEvents()
        }
    }
}

fun main() {
    SharedLibraryLoader.load()
    Lwjgl3Test().run()
}