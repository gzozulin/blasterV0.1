package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import org.joml.Vector2f
import org.joml.Vector3f

private val assetStream = AssetStream()
private val textureLib = TexturesLib(assetStream)
private val shadersLib = ShadersLib(assetStream)

private val glState = GlState()

// todo: Console class: line timeout, error - red, warning - yellow, info - white, success - green, rainbow :)

class TextTechnique {
    private lateinit var program: GlProgram
    private lateinit var font: GlTexture
    private lateinit var rect: GlMesh

    fun prepare() {
        program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
        font = textureLib.loadTexture("textures/lumina.png")
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

private val technique = TextTechnique()

private val window = object : LwjglWindow(800, 600) {
    override fun onCreate() {
        technique.prepare()
        glState.apply()
    }

    override fun onDraw() {
        glState.clear()
        technique.draw {
            technique.text("Hello, World!", Vector2f(-0.7f, 0.0f), 0.1f, Vector3f(0f, 1f, 0f))
        }
    }
}

fun main() {
    window.show()
}