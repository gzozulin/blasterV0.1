package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.Console
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.techniques.TextTechnique
import org.lwjgl.glfw.GLFW
import java.lang.IllegalArgumentException
import java.util.*

private val assetStream = AssetStream()
private val textureLib = TexturesLib(assetStream)
private val shadersLib = ShadersLib(assetStream)

private val technique = TextTechnique()
private val console = Console(2000L)

private val random = Random()

private val window = object : LwjglWindow(800, 600) {
    override fun onCreate(width: Int, height: Int) {
        technique.prepare(shadersLib, textureLib)
        GlState.apply()
    }

    override fun onDraw() {
        GlState.clear()
        console.tick()
        technique.draw {
            console.render { position, text, color, scale ->
                technique.text(text, position, scale, color)
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
