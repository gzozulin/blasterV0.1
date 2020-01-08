package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.scene.Model
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.techniques.DeferredTechnique

private const val WIDTH = 800
private const val HEIGHT = 600

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val deferredTechnique = DeferredTechnique()

private var camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())

private lateinit var model: Model

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        GlState.apply()
        deferredTechnique.prepare(shadersLib, WIDTH, HEIGHT)
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        camera.lookAt(model.aabb)
    }

    override fun onDraw() {
        model.tick()
        GlState.clear()
        deferredTechnique.draw(camera) {
            deferredTechnique.instance(model.mesh, model.diffuse, model.calculateModelM())
        }
    }
}

fun main() {
    window.show()
}