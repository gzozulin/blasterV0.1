package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.gl.GlMesh
import org.joml.Vector2f
import org.joml.Vector3f

class TextTechnique {
    private lateinit var program: GlProgram
    private lateinit var diffuse: GlTexture
    private lateinit var rect: GlMesh

    fun create(shadersLib: ShadersLib, textureLib: TexturesLib, font: String = "textures/font.png") {
        program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
        diffuse = textureLib.loadTexture(font)
        rect = GlMesh.rect()
    }

    fun draw(call: () -> Unit) {
        glBind(listOf(program, diffuse, rect)) {
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            call.invoke()
        }
    }

    private val startBuf = Vector2f()
    fun text(text: String, start: Vector2f, scale: Float, color: Vector3f) {
        text.forEachIndexed { index, ch ->
            startBuf.set(start.x + index * scale, start.y)
            character(ch, startBuf, scale, color)
        }
    }

    fun character(ch: Char, start: Vector2f, scale: Float, color: Vector3f) {
        program.setUniform(GlUniform.UNIFORM_CHAR_INDEX, ch.toInt())
        program.setUniform(GlUniform.UNIFORM_CHAR_START, start)
        program.setUniform(GlUniform.UNIFORM_CHAR_SCALE, scale)
        program.setUniform(GlUniform.UNIFORM_COLOR, color)
        rect.draw()
    }
}