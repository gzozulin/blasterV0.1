package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.VECTOR_UP
import com.blaster.gl.GlMesh
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.scene.Model
import com.blaster.techniques.SimpleTechnique
import org.joml.Math
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import kotlin.math.cos
import kotlin.math.sin

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

private val cameraController = Controller()

private val camera: Camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        cameraController.position.set(Vector3f(0f, 0f, 3f))
        simpleTechnique.prepare(shadersLib)
        mesh = GlMesh.triangle()
        tex1 = texturesLib.loadTexture("textures/lumina.png")
        tex2 = texturesLib.loadTexture("textures/utah.jpeg")
        tex3 = texturesLib.loadTexture("textures/winner.png")
        model1 = Model(mesh, tex1)
        model2 = Model(mesh, tex2)
        model3 = Model(mesh, tex3)
        model1.attach(model2)
        GlState.apply(false)
    }

    override fun onDraw() {
        cameraController.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        model1.tick()
        model2.tick()
        GlState.clear()
        simpleTechnique.draw(camera) {
            simpleTechnique.instance(model1)
            simpleTechnique.instance(model2)
            simpleTechnique.instance(model3)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        cameraController.yaw(delta.x)
        cameraController.pitch(-delta.y)
    }

    override fun keyPressed(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> cameraController.w = true
            GLFW.GLFW_KEY_A -> cameraController.a = true
            GLFW.GLFW_KEY_S -> cameraController.s = true
            GLFW.GLFW_KEY_D -> cameraController.d = true
        }
    }

    override fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> cameraController.w = false
            GLFW.GLFW_KEY_A -> cameraController.a = false
            GLFW.GLFW_KEY_S -> cameraController.s = false
            GLFW.GLFW_KEY_D -> cameraController.d = false
        }
    }
}

fun main() {
    window.show()
}
