package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
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

private val controller = Controller()

private val node1 = Node()
private val node2 = Node(node1)
private val node3 = Node(node2)

private lateinit var camera: Camera

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

        GlState.apply(false)
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        node1.tick()
        node2.tick()
        node3.tick()
        GlState.clear()
        simpleTechnique.draw(camera) {
            simpleTechnique.instance(model1, node1)
            simpleTechnique.instance(model2, node2)
            simpleTechnique.instance(model3, node3)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        controller.yaw(delta.x)
        controller.pitch(-delta.y)
    }

    override fun keyPressed(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.w = true
            GLFW.GLFW_KEY_A -> controller.a = true
            GLFW.GLFW_KEY_S -> controller.s = true
            GLFW.GLFW_KEY_D -> controller.d = true
            GLFW.GLFW_KEY_E -> controller.e = true
            GLFW.GLFW_KEY_Q -> controller.q = true
        }
    }

    override fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.w = false
            GLFW.GLFW_KEY_A -> controller.a = false
            GLFW.GLFW_KEY_S -> controller.s = false
            GLFW.GLFW_KEY_D -> controller.d = false
            GLFW.GLFW_KEY_E -> controller.e = false
            GLFW.GLFW_KEY_Q -> controller.q = false
        }
    }
}

fun main() {
    window.show()
}
