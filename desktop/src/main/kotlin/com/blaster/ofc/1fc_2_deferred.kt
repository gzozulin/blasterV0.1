package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ModelsLib
import com.blaster.assets.PixelDecoder
import com.blaster.assets.TexturesLib
import com.blaster.gl.GLModel
import com.blaster.platform.LwjglWindow
import com.blaster.renderers.DeferredRenderer

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val renderer = DeferredRenderer(assetStream)

private lateinit var model: GLModel

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        renderer.onCreate()
        renderer.onChange(width, height)
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        renderer.root.attach(model)
        renderer.camera.lookAt(model.aabb)
    }

    override fun onDraw() {
        model.tick()
        renderer.onDraw()
    }
}

fun main() {
    window.show()
}