package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.common.Console
import com.blaster.common.mat4
import com.blaster.common.random
import com.blaster.common.vec3
import com.blaster.entity.Light
import com.blaster.entity.Material
import com.blaster.entity.Model
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.*
import com.blaster.techniques.DeferredTechnique
import com.blaster.techniques.TextTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

private val assetStream = AssetStream()
private val pixelDecoder = PixelDecoder()

private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream, pixelDecoder)
private val meshLib = MeshLib(assetStream)

private val deferredTechnique = DeferredTechnique()
private val textTechnique = TextTechnique()

private val console = Console(3000L)

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.05f)
private val wasd = WasdInput(controller)

private lateinit var model: Model

private var currentMaterial = 0

private fun nextMaterial() {
    currentMaterial++
    if (currentMaterial == Material.MATERIALS.size) {
        currentMaterial = 0
    }
    console.success("Material: ${Material.MATERIALS[currentMaterial].first}")
}

private fun prevMaterial() {
    currentMaterial--
    if (currentMaterial < 0) {
        currentMaterial = Material.MATERIALS.size - 1
    }
    console.success("Material: ${Material.MATERIALS[currentMaterial].first}")
}

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        camera = Camera(width.toFloat() / height.toFloat())
        controller.position.set(Vector3f(0.5f, 3f, 3f))
        val (mesh, aabb) = meshLib.loadMesh("models/house/low.obj")
        val diffuse = texturesLib.loadTexture("models/house/house_diffuse.png")
        model = Model(mesh, diffuse, aabb)
        GlState.apply()
        textTechnique.prepare(shadersLib, texturesLib)
        deferredTechnique.prepare(shadersLib, width, height)
        deferredTechnique.light(Light.SUNLIGHT)
        for (i in 0..16) {
            deferredTechnique.light(Light(
                    vec3().random(vec3(model.aabb.minX - 1f, model.aabb.minY, model.aabb.minZ),
                            vec3(model.aabb.maxX + 1f, model.aabb.maxY, model.aabb.maxZ)),
                    vec3().random(max = Vector3f(2f))))
        }
    }

    override fun onDraw() {
        console.tick()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        textTechnique.draw {
            console.render { pos, text, color, scale ->
                textTechnique.text(text, pos, scale, color)
            }
        }
        deferredTechnique.draw(camera) {
            deferredTechnique.instance(model.mesh, mat4(), model.diffuse, Material.MATERIALS[currentMaterial].second)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
        when (key) {
            GLFW.GLFW_KEY_LEFT_BRACKET -> prevMaterial()
            GLFW.GLFW_KEY_RIGHT_BRACKET -> nextMaterial()
        }
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}