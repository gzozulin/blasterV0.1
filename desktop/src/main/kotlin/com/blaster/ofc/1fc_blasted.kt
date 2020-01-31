package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.*
import com.blaster.editor.MultiListener
import com.blaster.editor.SceneDiffer
import com.blaster.editor.SceneReader
import com.blaster.entity.Light
import com.blaster.entity.Marker
import com.blaster.entity.Material
import com.blaster.entity.Model
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.scene.Node
import com.blaster.techniques.DeferredTechnique
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.SkyboxTechnique
import com.blaster.techniques.TextTechnique
import org.lwjgl.glfw.GLFW
import java.io.File

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val deferredTechnique = DeferredTechnique()
private val immediateTechnique = ImmediateTechnique()
private val textTechnique = TextTechnique()
private val skyboxTechnique = SkyboxTechnique()

private val console = Console(5000L)

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private val sunlight = Light(color(0.8f), point = false)
private val sunlightNode = Node(payload = sunlight).lookAlong(vec3(-1f))
private val lightNodes = mutableMapOf<String, Node<Light>>()

private lateinit var baseModel: Model

private val teapotNodes = mutableMapOf<String, Node<Model>>()

private var lastUpdate = 0L
private var currentScene = listOf<Marker>()

private var mouseControl = false
private var showImmediate = true

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

private val cameraListener = object : SceneDiffer.Listener() {
    override fun onAdd(marker: Marker) {
        marker.apply(controller)
    }

    override fun onUpdate(marker: Marker) {
        marker.apply(controller)
    }
}

private val lightListener = object : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        val node = lightNodes[marker.uid]!!
        node.detachFromParent()
        lightNodes.remove(marker.uid)
    }

    override fun onAdd(marker: Marker) {
        val node = Node(payload = Light(intensity(marker), point = true))
        marker.apply(node)
        lightNodes[marker.uid] = node
    }

    override fun onUpdate(marker: Marker) {
        val node = lightNodes[marker.uid]!!
        marker.apply(node)
        val light = node.payload as Light
        light.intensity.set(intensity(marker))
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        val node = lightNodes[marker.uid]!!
        if (parent != null) {
            lightNodes[parent.uid]!!.attach(node)
        } else {
            node.detachFromParent()
        }
    }

    private fun intensity(marker: Marker) = marker.custom.first().toVec3()
}

private val teapotListener = object : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        val node = teapotNodes[marker.uid]!!
        node.detachFromParent()
        teapotNodes.remove(marker.uid)
    }

    override fun onAdd(marker: Marker) {
        val material = if (marker.custom.isNotEmpty()) Material.MATERIALS.getValue(marker.custom.first()) else Material.CONCRETE
        val node = Node(payload = baseModel.copy(material = material))
        marker.apply(node)
        teapotNodes[marker.uid] = node
    }

    override fun onUpdate(marker: Marker) {
        val node = teapotNodes[marker.uid]!!
        marker.apply(node)
        val bound = marker.bound
        if (bound != null) {
            val model = node.payload as Model
            node.setScale(model.aabb.scaleTo(bound))
        }
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        val node = teapotNodes[marker.uid]!!
        if (parent != null) {
            teapotNodes[parent.uid]!!.attach(node)
        } else {
            node.detachFromParent()
        }
    }
}

private val sceneListener = MultiListener(console = console, listeners = mapOf(
        "camera" to cameraListener,
        "teapot" to teapotListener,
        "light" to lightListener
))

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        camera = Camera(width.toFloat() / height.toFloat())
        deferredTechnique.prepare(shadersLib, width, height)
        immediateTechnique.prepare(camera)
        textTechnique.prepare(shadersLib, texturesLib)
        skyboxTechnique.prepare(shadersLib, texturesLib, meshLib, "textures/gatekeeper")
        controller.position.set(vec3(0.5f, 3f, 3f))
        val (mesh, aabb) = meshLib.loadMesh("models/teapot/teapot.obj")
        val diffuse = texturesLib.loadTexture("textures/marble.jpeg")
        baseModel = Model(mesh, diffuse, aabb, Material.CONCRETE)
        updateLights()
    }

    private fun updateLights() {
        lightNodes["lightDynamic"]?.setPosition(vec3(0f, randomFloat(0f, 2f), 0f))
        val data = mutableListOf<DeferredTechnique.LightData>()
        lightNodes.forEach {
            data.add(DeferredTechnique.LightData(it.value.payload!!, it.value.calculateM()))
        }
        deferredTechnique.setLights(data)
        deferredTechnique.light(sunlight, sunlightNode.calculateM())
    }

    private fun tick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate > 1000L) {
            try {
                val nextScene = sceneReader.load(File("scene_file").inputStream())
                sceneDiffer.diff(prevMarkers = currentScene, nextMarkers = nextScene, listener = sceneListener)
                currentScene = nextScene
            } catch (e: Exception) {
                console.failure(e.message!!)
            }
            updateLights()
            lastUpdate = currentTime
        }
        console.tick()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    private fun draw() {
        GlState.clear()
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
            }
        }
        deferredTechnique.draw(camera) {
            for (node in teapotNodes.values) {
                val model = node.payload!!
                deferredTechnique.instance(model.mesh, node.calculateM(), model.diffuse, model.material)
            }
        }
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
        if (showImmediate) {
            GlState.drawWithNoDepth {
                immediateTechnique.marker(camera, mat4(),
                        color1 = color(1f, 0f, 0f), color2 = color(0f, 1f, 0f), color3 = color(0f, 0f, 1f), scale = 5f)
                teapotNodes.values.forEach {
                    immediateTechnique.aabb(camera, it.payload!!.aabb, it.calculateM(), color(1f, 0f, 0f))
                }
                lightNodes.values.forEach {
                    val light = it.payload as Light
                    immediateTechnique.marker(camera, it.calculateM(), light.intensity)
                }
            }
        }
    }

    override fun onTick() {
        tick()
        draw()
    }

    override fun mouseBtnPressed(btn: Int) {
        if (btn == GLFW.GLFW_MOUSE_BUTTON_1) {
            mouseControl = true
        }
    }

    override fun mouseBtnReleased(btn: Int) {
        if (btn == GLFW.GLFW_MOUSE_BUTTON_1) {
            mouseControl = false
        }
    }

    override fun onCursorDelta(delta: vec2) {
        if (mouseControl) {
            wasd.onCursorDelta(delta)
        }
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
        when (key) {
            GLFW.GLFW_KEY_F1 -> console.success("Cam pos: (%.1f, %.1f, %.1f), euler: (%.1f, %.1f, %.1f)".format(
                    controller.position.x, controller.position.y, controller.position.z,
                    degf(controller.pitch), degf(controller.yaw), degf(controller.roll)))
            GLFW.GLFW_KEY_F2 -> showImmediate = !showImmediate
        }
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}
