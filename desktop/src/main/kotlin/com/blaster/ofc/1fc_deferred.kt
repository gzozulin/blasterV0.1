package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.scene.Model
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.techniques.DeferredTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

private const val WIDTH = 800
private const val HEIGHT = 600

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val deferredTechnique = DeferredTechnique()

private val cameraController = Controller(velocity = 0.05f)

private var camera = Camera(WIDTH.toFloat() / HEIGHT.toFloat())

private lateinit var model: Model

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        cameraController.position.set(Vector3f(0.5f, 3f, 3f))
        GlState.apply()
        deferredTechnique.prepare(shadersLib, WIDTH, HEIGHT)
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
    }

    override fun onDraw() {
        cameraController.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        deferredTechnique.draw(camera) {
            deferredTechnique.instance(model.mesh, model.diffuse, model.calculateModelM())
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
            GLFW.GLFW_KEY_E -> cameraController.e = true
            GLFW.GLFW_KEY_Q -> cameraController.q = true
        }
    }

    override fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> cameraController.w = false
            GLFW.GLFW_KEY_A -> cameraController.a = false
            GLFW.GLFW_KEY_S -> cameraController.s = false
            GLFW.GLFW_KEY_D -> cameraController.d = false
            GLFW.GLFW_KEY_E -> cameraController.e = false
            GLFW.GLFW_KEY_Q -> cameraController.q = false
        }
    }
}

fun main() {
    window.show()
}