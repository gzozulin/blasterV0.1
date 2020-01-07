package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.Console
import com.blaster.common.extractColors
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.techniques.TextTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.lang.IllegalArgumentException
import java.util.*

private val assetStream = AssetStream()
private val textureLib = TexturesLib(assetStream)
private val shadersLib = ShadersLib(assetStream)

private val glState = GlState()

private val technique = TextTechnique()
private val console = Console(2000L)

private val COLOR_FAILURE = extractColors("ffabab")
private val COLOR_INFO = extractColors("6eb5ff")
private val COLOR_SUCCESS = extractColors("9ee09e")

private const val TEXT_SCALE = 0.025f

private val random = Random()

private val window = object : LwjglWindow(800, 600) {
    override fun onCreate() {
        technique.prepare(shadersLib, textureLib)
        glState.apply()
    }

    override fun onDraw() {
        glState.clear()
        console.throttle()
        technique.draw {
            console.render { index, text, level ->
                val color = when (level) {
                    Console.Level.FAILURE -> COLOR_FAILURE
                    Console.Level.INFO -> COLOR_INFO
                    Console.Level.SUCCESS -> COLOR_SUCCESS
                }
                technique.text(text, Vector2f(-0.8f, 0.8f - TEXT_SCALE * index * 2f), TEXT_SCALE, color)
            }
        }
    }

    override fun keyPressed(key: Int) {
        if (key == GLFW.GLFW_KEY_SPACE) {
            when (random.nextInt(3)) {
                0 -> console.failure(System.currentTimeMillis().toString())
                1 -> console.info(System.currentTimeMillis().toString())
                2 -> console.success(System.currentTimeMillis().toString())
                else -> throw IllegalArgumentException()
            }
        }
    }
}

fun main() {
    window.show()
}
