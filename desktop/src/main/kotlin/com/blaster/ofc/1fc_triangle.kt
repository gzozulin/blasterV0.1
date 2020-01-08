package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Model
import com.blaster.techniques.SimpleTechnique
import org.joml.Vector3f

private const val WIDTH = 800
private const val HEIGHT = 600

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val shadersLib = ShadersLib(assetStream)

private val simpleTechnique = SimpleTechnique()

private lateinit var mesh: GlMesh
private lateinit var tex1: GlTexture
private lateinit var tex2: GlTexture
private lateinit var tex3: GlTexture
private lateinit var model1: Model
private lateinit var model2: Model
private lateinit var model3: Model

private val camera: Camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())
        .lookAt(Vector3f(0f, 0f, 2.5f), Vector3f())

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        simpleTechnique.prepare(shadersLib)
        mesh = GlMesh.triangle()
        tex1 = texturesLib.loadTexture("textures/lumina.png")
        tex2 = texturesLib.loadTexture("textures/utah.jpeg")
        tex3 = texturesLib.loadTexture("textures/winner.png")
        model1 = Model(mesh, tex1)
        model2 = Model(mesh, tex2)
        model3 = Model(mesh, tex3)
        model1.attach(model2)
        //model2.attach(model3)
        GlState.apply(false)
    }

    override fun onDraw() {
        model1.tick()
        model2.tick()
        //model3.tick()
        GlState.clear()
        simpleTechnique.draw(camera) {
            simpleTechnique.instance(model1)
            simpleTechnique.instance(model2)
            simpleTechnique.instance(model3)
        }
    }
}

fun main() {
    window.show()
}
