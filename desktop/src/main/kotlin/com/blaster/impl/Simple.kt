package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.auxiliary.vec3
import com.blaster.entity.Camera
import com.blaster.entity.Controller
import com.blaster.entity.Model
import com.blaster.entity.Node
import com.blaster.gl.GlMesh
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.techniques.SimpleTechnique
import org.joml.Vector2f
import org.joml.Vector3f

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

private lateinit var node1: Node<Model>
private lateinit var node2: Node<Model>
private lateinit var node3: Node<Model>

private val camera = Camera()
private val controller = Controller(Vector3f(0f, 0f, 3f))
private val wasd = WasdInput(controller)

private val axis = vec3(1f)

private var mouseMove = false

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        // Bootstrapping our technique first
        simpleTechnique.create(shadersLib)
        // Creating a mesh and textures
        mesh = GlMesh.triangle()
        tex1 = texturesLib.loadTexture("textures/lumina.png")
        tex2 = texturesLib.loadTexture("textures/utah.jpeg")
        tex3 = texturesLib.loadTexture("textures/winner.png")
        model1 = Model(mesh, tex1)
        model2 = Model(mesh, tex2)
        model3 = Model(mesh, tex3)
        // Creating separate nodes to track three instances in space
        node1 = Node(payload = model1)
        node2 = Node(parent = node1, payload = model2)
        node3 = Node(parent = node2, payload = model3)
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height)
        camera.setPerspective(width, height)
    }

    override fun onTick() {
        GlState.clear()
        // Applying update from WASD controller
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        // Adding some animation to the scene
        node1.rotate(axis, 0.01f)
        node2.rotate(axis, 0.01f)
        node3.rotate(axis, 0.01f)
        // Drawing instances with our technique
        GlState.drawWithNoCulling {
            simpleTechnique.draw(camera) {
                simpleTechnique.instance(model1, node1.calculateM())
                simpleTechnique.instance(model2, node2.calculateM())
                simpleTechnique.instance(model3, node3.calculateM())
            }
        }
    }

    override fun mouseBtnPressed(btn: Int) {
        mouseMove = true
    }

    override fun mouseBtnReleased(btn: Int) {
        mouseMove = false
    }

    override fun onCursorDelta(delta: Vector2f) {
        if (mouseMove) {
            wasd.onCursorDelta(delta)
        }
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
