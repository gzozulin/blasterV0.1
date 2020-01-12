package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.scene.Mesh
import org.joml.Vector2f
import org.joml.Vector3f

class TextTechnique {
    private lateinit var program: GlProgram
    private lateinit var font: GlTexture
    private lateinit var rect: Mesh

    fun prepare(shadersLib: ShadersLib, textureLib: TexturesLib) {
        program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
        font = textureLib.loadTexture("textures/font.png")
        rect = Mesh.rect()
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