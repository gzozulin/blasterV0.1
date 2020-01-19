package com.blaster.ofc

import com.blaster.assets.*
import com.blaster.common.Console
import com.blaster.common.random
import com.blaster.common.vec3
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
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
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val deferredTechnique = DeferredTechnique()
private val textTechnique = TextTechnique()

private val console = Console(3000L)

private val controller = Controller(velocity = 0.05f)

private lateinit var camera: Camera

private lateinit var model: Model
private val node = Node()

private val materials = listOf(
        "concrete" to Material.CONCRETE,
        "brass" to Material.BRASS,
        "bronze" to Material.BRONZE,
        "polished bronze" to Material.POLISHED_BRONZE,
        "chrome" to Material.CHROME,
        "copper" to Material.COPPER,
        "polished copper" to Material.POLISHED_COPPER,
        "gold" to Material.GOLD,
        "polished gold" to Material.POLISHED_GOLD,
        "tin" to Material.TIN,
        "silver" to Material.SILVER,
        "polished silver" to Material.POLISHED_SILVER,
        "emerald" to Material.EMERALD,
        "jade" to Material.JADE,
        "obsidian" to Material.OBSIDIAN,
        "perl" to Material.PERL,
        "ruby" to Material.RUBY)
private var currentMaterial = 0

private fun nextMaterial() {
    currentMaterial++
    if (currentMaterial == materials.size) {
        currentMaterial = 0
    }
    console.success("Material: ${materials[currentMaterial].first}")
}

private fun prevMaterial() {
    currentMaterial--
    if (currentMaterial < 0) {
        currentMaterial = materials.size - 1
    }
    console.success("Material: ${materials[currentMaterial].first}")
}

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        camera = Camera(width.toFloat() / height.toFloat())
        controller.position.set(Vector3f(0.5f, 3f, 3f))
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
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
            deferredTechnique.instance(model.mesh, node.calculateModelM(), model.diffuse, materials[currentMaterial].second)
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
            GLFW.GLFW_KEY_LEFT_BRACKET -> prevMaterial()
            GLFW.GLFW_KEY_RIGHT_BRACKET -> nextMaterial()
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