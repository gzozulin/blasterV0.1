package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow

private val assetStream = AssetStream()
private val textureLib = TexturesLib(assetStream)
private val shadersLib = ShadersLib(assetStream)

private val glState = GlState()

class TextTechnique {
    private lateinit var program: GlProgram
    private lateinit var font: GlTexture
    private lateinit var rect: GlMesh

    private val table = mapOf(
            'A' to 177,
            'B' to 178,
            'C' to 179,
            'D' to 180,
            'E' to 181,
            'F' to 182,
            'G' to 183,
            'H' to 184,
            'I' to 185,
            'J' to 186,
            'K' to 187,
            'L' to 188,
            'L' to 189,
            'M' to 190,
            'N' to 191,
            'O' to 192,
            'P' to 193,
            'Q' to 194,
            'R' to 195,
            'S' to 196,
            'T' to 197,
            'U' to 198,
            'V' to 199,
            'W' to 200,
            'X' to 201,
            'Y' to 202,
            'Z' to 203)

    fun prepare() {
        program = shadersLib.loadProgram("shaders/text/text.vert", "shaders/text/text.frag")
        font = textureLib.loadTexture("textures/lumina.png")
        rect = GlMesh.rectPosTex()
    }

    fun draw(call: () -> Unit) {
        glBind(listOf(program, font, rect)) {
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, font)
            call.invoke()
        }
    }

    fun character(ch: Char) {
        program.setUniform(GlUniform.UNIFORM_CHAR_INDEX,
                table[ch] ?: error("Symbol is not found in the table: $ch"))
        rect.draw()
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
            technique.character('W')
        }
    }
}

fun main() {
    window.show()
}