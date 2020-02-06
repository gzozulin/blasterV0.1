package com.blaster.impl

import com.blaster.assets.*
import com.blaster.common.*
import com.blaster.entity.Light
import com.blaster.entity.Material
import com.blaster.entity.Model
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.*
import com.blaster.techniques.DeferredTechnique
import com.blaster.techniques.SkyboxTechnique
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
private val skyboxTechnique = SkyboxTechnique()

private val console = Console(3000L)

private val camera = Camera()
private val controller = Controller(Vector3f(0.5f, 3f, 3f), velocity = 0.05f)
private val wasd = WasdInput(controller)

private lateinit var model: Model

private val sunlight = Light(color(3f), point = false)
private val sunlightNode = Node(payload = sunlight).lookAlong(vec3(-1f))

private var materialIterator = Material.MATERIALS.iterator()
private var currentMaterial = materialIterator.next()

private fun nextMaterial() {
    if (materialIterator.hasNext()) {
        currentMaterial = materialIterator.next()
    } else {
        materialIterator = Material.MATERIALS.iterator()
        currentMaterial = materialIterator.next()
    }
    console.success("Material: ${currentMaterial.key}")
}

private val window = object : LwjglWindow() {
    override fun onCreate() {
        val (mesh, aabb) = meshLib.loadMesh("models/house/low.obj")
        val diffuse = texturesLib.loadTexture("models/house/house_diffuse.png")
        model = Model(mesh, diffuse, aabb)
        textTechnique.create(shadersLib, texturesLib)
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/hangingstone")
        deferredTechnique.create(shadersLib)
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height)
        deferredTechnique.resize(width, height)
        camera.setPerspective(width, height)
    }

    private fun tick() {
        console.tick()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    private fun draw() {
        GlState.clear()
        textTechnique.draw {
            console.render { pos, text, color, scale ->
                textTechnique.text(text, pos, scale, color)
            }
        }
        deferredTechnique.draw(camera, meshes =  {
            deferredTechnique.instance(model.mesh, mat4(), model.diffuse, currentMaterial.value)
        }, lights = {
            deferredTechnique.light(sunlight, sunlightNode.calculateM())
        })
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
    }

    override fun onTick() {
        tick()
        draw()
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
        when (key) {
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