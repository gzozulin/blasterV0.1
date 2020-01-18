package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.scene.*
import com.blaster.techniques.DeferredTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val deferredTechnique = DeferredTechnique()

private val controller = Controller(velocity = 0.05f)

private lateinit var camera: Camera

private lateinit var model: Model

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        camera = Camera(width.toFloat() / height.toFloat())
        controller.position.set(Vector3f(0.5f, 3f, 3f))
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        GlState.apply()
        deferredTechnique.prepare(shadersLib, width, height)
        deferredTechnique.light(Light.SUNLIGHT)
        /*for (i in 0..15) {
            deferredTechnique.light(Light(
                            randomVector3f(Vector3f(model.aabb.minX - 1f, model.aabb.minY, model.aabb.minZ),
                                    Vector3f(model.aabb.maxX + 1f, model.aabb.maxY, model.aabb.maxZ)),
                            randomVector3f(Vector3f(), Vector3f(2f))))
        }*/
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        deferredTechnique.draw(camera) {
            deferredTechnique.instance(model.mesh, model.calculateModelM(), model.diffuse, Material.CHROME)
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