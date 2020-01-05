package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.gl.GlModel
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Node
import com.blaster.techniques.DeferredTechnique

private const val WIDTH = 800
private const val HEIGHT = 600

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val glState = GlState()

private val deferredTechnique = DeferredTechnique()

private var camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())

private lateinit var model: GlModel
private lateinit var renderlist: List<Node>

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        glState.apply()
        deferredTechnique.prepare(shadersLib, WIDTH, HEIGHT)
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        renderlist = listOf(model)
        camera.lookAt(model.aabb)
    }

    override fun onDraw() {
        model.tick()
        deferredTechnique.draw(camera, renderlist)
    }
}

fun main() {
    window.show()
}