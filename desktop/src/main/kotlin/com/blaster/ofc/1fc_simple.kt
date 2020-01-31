package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.VECTOR_UP
import com.blaster.common.aabb
import com.blaster.common.vec3
import com.blaster.entity.Model
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.*
import com.blaster.techniques.SimpleTechnique
import org.joml.Vector2f
import org.joml.Vector3f

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

private val axis = vec3(1f)

private val window = object : LwjglWindow() {
    override fun onCreate() {
        //GlState.apply(width, height)
        //camera = Camera(width.toFloat() / height.toFloat())
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
    }

    override fun onResize(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTick() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        node1.rotate(axis, 0.01f)
        node2.rotate(axis, 0.01f)
        node3.rotate(axis, 0.01f)
        GlState.clear()
        GlState.drawWithNoCulling {
            simpleTechnique.draw(camera) {
                simpleTechnique.instance(model1, node1.calculateM())
                simpleTechnique.instance(model2, node2.calculateM())
                simpleTechnique.instance(model3, node3.calculateM())
            }
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
