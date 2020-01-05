package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Node
import com.blaster.techniques.SimpleTechnique
import org.joml.Vector3f

private const val WIDTH = 800
private const val HEIGHT = 600

private val glState = GlState()

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val shadersLib = ShadersLib(assetStream)

private val simpleTechnique = SimpleTechnique()

private val triangleAttributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD)
private val triangleVertices = floatArrayOf(
        0f,  1f, 0f,     0.5f, 0f,
        -1f, -1f, 0f,     0f,   1f,
        1f, -1f, 0f,     1f,   1f
)
private val triangleIndices = intArrayOf(0, 1, 2)

private lateinit var mesh: GlMesh
private lateinit var tex1: GlTexture
private lateinit var tex2: GlTexture
private lateinit var model1: GlModel
private lateinit var model2: GlModel

private val camera: Camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())
        .lookAt(Vector3f(0f, 0f, 2.5f), Vector3f())

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        simpleTechnique.prepare(shadersLib)
        mesh = GlMesh(triangleVertices, triangleIndices, triangleAttributes)
        tex1 = texturesLib.loadTexture("textures/lumina.png")
        tex2 = texturesLib.loadTexture("textures/utah.jpeg")
        model1 = GlModel(mesh, tex1)
        model2 = GlModel(mesh, tex2)
        model1.attach(model2)
        glState.apply(false)
    }

    override fun onDraw() {
        model1.tick()
        model2.tick()
        glState.clear()
        simpleTechnique.draw(camera) {
            simpleTechnique.instance(model1)
            simpleTechnique.instance(model2)
        }
    }
}

fun main() {
    window.show()
}
