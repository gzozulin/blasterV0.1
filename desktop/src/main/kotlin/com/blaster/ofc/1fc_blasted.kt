package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.*
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
import java.lang.IllegalStateException

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

private lateinit var baseModel: Model

private val nodes = mutableMapOf<String, Node<Model>>()

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

}

private val teapotListener = object : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        val node = nodes[marker.uid]!!
        node.detachFromParent()
        nodes.remove(marker.uid)
    }

    override fun onAdd(marker: Marker) {
        val material = if (marker.custom.isNotEmpty()) Material.MATERIALS.getValue(marker.custom.first()) else Material.CONCRETE
        val node = Node(payload = baseModel.copy(material = material))
        marker.apply(node)
        nodes[marker.uid] = node
    }

    override fun onUpdate(marker: Marker) {
        val node = nodes[marker.uid]!!
        marker.apply(node)
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        if (parent != null) {
            nodes[parent.uid]!!.attach(nodes[marker.uid]!!)
        } else {
            nodes[marker.uid]!!.detachFromParent()
        }
    }
}

private class MultiListener(private val console: Console? = null, private val listeners: Map<String, SceneDiffer.Listener>)
    : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        getListener(marker).onRemove(marker)
        console?.info("Marker removed: ${marker.uid}")
    }

    override fun onUpdate(marker: Marker) {
        getListener(marker).onUpdate(marker)
        console?.info("Marker updated: ${marker.uid}")
    }

    override fun onAdd(marker: Marker) {
        getListener(marker).onAdd(marker)
        console?.info("Marker added: ${marker.uid}")
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        getListener(marker).onParent(marker, parent)
        console?.info("Marker ${marker.uid} attached to ${parent?.uid}")
    }

    private fun getListener(marker: Marker): SceneDiffer.Listener {
        listeners.forEach {
            if (marker.uid.startsWith(it.key)) {
                return it.value
            }
        }
        throw IllegalStateException("Listener is not registered! ${marker.uid}")
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
        controller.position.set(vec3(0.5f, 3f, 3f))
        val (mesh, aabb) = meshLib.loadMesh("models/teapot/teapot.obj")
        val diffuse = texturesLib.loadTexture("textures/marble.jpeg")
        baseModel = Model(mesh, diffuse, aabb, Material.CONCRETE)
        deferredTechnique.prepare(shadersLib, width, height)
        camera = Camera(width.toFloat() / height.toFloat())
        deferredTechnique.light(sunlight, sunlightNode.calculateM())
        immediateTechnique.prepare(camera)
        textTechnique.prepare(shadersLib, texturesLib)
        skyboxTechnique.prepare(shadersLib, texturesLib, meshLib, "textures/gatekeeper")
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
        if (showImmediate) {
            immediateTechnique.marker(camera, vec3(), mat4(),
                    color1 = color(1f, 0f, 0f), color2 = color(0f, 1f, 0f), color3 = color(0f, 0f, 1f), scale = 5f)
            nodes.values.forEach {
                immediateTechnique.aabb(camera, it.payload!!.aabb, it.calculateM(), color(1f, 0f, 0f))
            }
        }
        deferredTechnique.draw(camera) {
            for (node in nodes.values) {
                val model = node.payload!!
                deferredTechnique.instance(model.mesh, node.calculateM(), model.diffuse, model.material)
            }
        }
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
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
