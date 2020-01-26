package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.VECTOR_UP
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.*
import com.blaster.techniques.SimpleTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val shadersLib = ShadersLib(assetStream)

private val simpleTechnique = SimpleTechnique()

private lateinit var mesh: Mesh
private lateinit var tex1: GlTexture
private lateinit var tex2: GlTexture
private lateinit var tex3: GlTexture
private lateinit var model1: Model
private lateinit var model2: Model
private lateinit var model3: Model

private lateinit var node1: Node<Model>
private lateinit var node2: Node<Model>
private lateinit var node3: Node<Model>

private lateinit var camera: Camera
private val controller = Controller()
private val wasd = WasdInput(controller)

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        camera = Camera(width.toFloat() / height.toFloat())
        controller.position.set(Vector3f(0f, 0f, 3f))
        simpleTechnique.prepare(shadersLib)
        mesh = Mesh.triangle()
        tex1 = texturesLib.loadTexture("textures/lumina.png")
        tex2 = texturesLib.loadTexture("textures/utah.jpeg")
        tex3 = texturesLib.loadTexture("textures/winner.png")
        model1 = Model(mesh, tex1)
        model2 = Model(mesh, tex2)
        model3 = Model(mesh, tex3)
        node1 = Node(payload = model1)
        node2 = Node(parent = node1, payload = model2)
        node3 = Node(parent = node2, payload = model3)
        GlState.apply(false)
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        node1.rotate(VECTOR_UP, 0.01f)
        node2.rotate(VECTOR_UP, 0.01f)
        node3.rotate(VECTOR_UP, 0.01f)
        GlState.clear()
        simpleTechnique.draw(camera) {
            simpleTechnique.instance(model1, node1.calculateModelM())
            simpleTechnique.instance(model2, node2.calculateModelM())
            simpleTechnique.instance(model3, node3.calculateModelM())
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}
