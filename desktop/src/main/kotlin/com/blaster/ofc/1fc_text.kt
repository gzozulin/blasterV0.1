package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.extractColors
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.lang.IllegalArgumentException
import java.util.*

class TextTechnique {
    private lateinit var program: GlProgram
    private lateinit var font: GlTexture
    private lateinit var rect: GlMesh

    fun prepare() {
        program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
        font = textureLib.loadTexture("textures/font.png")
        rect = GlMesh.rect()
    }

    fun draw(call: () -> Unit) {
        glBind(listOf(program, font, rect)) {
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, font)
            call.invoke()
        }
    }

    fun character(ch: Char, start: Vector2f, scale: Float, color: Vector3f) {
        program.setUniform(GlUniform.UNIFORM_CHAR_INDEX, ch.toInt())
        program.setUniform(GlUniform.UNIFORM_CHAR_START, start)
        program.setUniform(GlUniform.UNIFORM_CHAR_SCALE, scale)
        program.setUniform(GlUniform.UNIFORM_COLOR, color)
        rect.draw()
    }

    private val startBuf = Vector2f()
    fun text(text: String, start: Vector2f, scale: Float, color: Vector3f) {
        text.forEachIndexed { index, ch ->
            startBuf.set(start.x + index * scale, start.y)
            character(ch, startBuf, scale, color)
        }
    }
}

class Console(private val timeout: Long = 1000L) {
    enum class Level { FAILURE, INFO, SUCCESS }
    private data class Line(val text: String, val timestamp: Long, val level: Level)
    private val lines = mutableListOf<Line>()

    fun line(text: String, level: Level) {
        lines.add(Line(text, System.currentTimeMillis(), level))
    }

    fun failure(text: String) {
        line(text, Level.FAILURE)
    }

    fun info(text: String) {
        line(text, Level.INFO)
    }

    fun success(text: String) {
        line(text, Level.SUCCESS)
    }

    fun throttle() {
        val current = System.currentTimeMillis()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (current - line.timestamp > timeout) {
                iterator.remove()
            }
        }
    }

    fun render(callback: (index: Int, text: String, level: Level) -> Unit) {
        lines.forEachIndexed { index, line ->
            callback.invoke(index, line.text, line.level)
        }
    }
}

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
        technique.prepare()
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
