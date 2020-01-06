package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

private val assetStream = AssetStream()
private val textureLib = TexturesLib(assetStream)
private val shadersLib = ShadersLib(assetStream)

private val glState = GlState()

// todo: Console class: line timeout, error - red, warning - yellow, info - blue, success - green, rainbow :)

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
    private data class Line(val text: String, val timestamp: Long)
    private val lines = mutableListOf<Line>()

    fun line(text: String) {
        lines.add(Line(text, System.currentTimeMillis()))
    }

    fun throttle() {
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val current = System.currentTimeMillis()
            val line = iterator.next()
            if (current - line.timestamp > timeout) {
                iterator.remove()
            }
        }
    }

    fun render(callback: (index: Int, text: String) -> Unit) {
        lines.forEachIndexed { index, line ->
            callback.invoke(index, line.text)
        }
    }
}

private val technique = TextTechnique()
private val console = Console(2000L)

const val TEXT_SCALE = 0.025f

private val window = object : LwjglWindow(800, 600) {
    override fun onCreate() {
        technique.prepare()
        glState.apply()
    }

    override fun onDraw() {
        glState.clear()
        console.throttle()
        technique.draw {
            console.render { index, text ->
                technique.text(text, Vector2f(-0.8f, 0.8f - TEXT_SCALE * index * 2f), TEXT_SCALE, Vector3f(0f, 1f, 0f))
            }
        }
    }

    override fun keyPressed(key: Int) {
        if (key == GLFW.GLFW_KEY_SPACE) {
            console.line(System.currentTimeMillis().toString())
        }
    }
}

fun main() {
    window.show()
}
