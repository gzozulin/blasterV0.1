package com.blaster.impl

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

private val camera = Camera()
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private val sunlight = Light(color(0.3f), point = false)
private val sunlightNode = Node(payload = sunlight).lookAlong(vec3(-1f))
private val lightNodes = mutableMapOf<String, Node<Light>>()

private lateinit var teapotModel: Model
private val teapotNodes = mutableMapOf<String, Node<Model>>()

private var lastUpdate = 0L
private var currentScene = listOf<Marker>()

private var mouseControl = false
private var showImmediate = false

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
        node.payload().intensity.set(intensity(marker))
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
        val node = Node(payload = teapotModel.copy(material = material))
        marker.apply(node)
        teapotNodes[marker.uid] = node
    }

    override fun onUpdate(marker: Marker) {
        val node = teapotNodes[marker.uid]!!
        marker.apply(node)
        val bound = marker.bound
        if (bound != null) {
            node.setScale(node.payload().aabb.scaleTo(bound))
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
    override fun onCreate() {
        deferredTechnique.create(shadersLib)
        textTechnique.create(shadersLib, texturesLib)
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/gatekeeper")
        val (mesh, aabb) = meshLib.loadMesh("models/teapot/teapot.obj")
        val diffuse = texturesLib.loadTexture("textures/marble.jpeg")
        teapotModel = Model(mesh, diffuse, aabb, Material.CONCRETE)
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height)
        camera.setPerspective(width.toFloat() / height.toFloat())
        deferredTechnique.resize(width, height)
        immediateTechnique.resize(camera)
    }

    private var value = 0f
    private fun tickLights() {
        value += 0.05f
        val dynamic = lightNodes["lightDynamic"]
        dynamic?.setPosition(vec3(sinf(value) * 3f, 0f, cosf(value) * 3f))
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
        tickLights()
    }

    private fun draw() {
        GlState.clear()
        deferredTechnique.draw(camera, meshes =  {
            for (node in teapotNodes.values) {
                val model = node.payload()
                deferredTechnique.instance(model.mesh, node.calculateM(), model.diffuse, model.material)
            }
        }, lights = {
            lightNodes.forEach {
                deferredTechnique.light(it.value.payload(), it.value.calculateM())
            }
            deferredTechnique.light(sunlight, sunlightNode.calculateM())
        })
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
        if (showImmediate) {
            GlState.drawWithNoDepth {
                immediateTechnique.marker(camera, mat4(),
                        color1 = color(1f, 0f, 0f), color2 = color(0f, 1f, 0f), color3 = color(0f, 0f, 1f), scale = 5f)
                teapotNodes.values.forEach {
                    immediateTechnique.aabb(camera, it.payload().aabb, it.calculateM(), color(1f, 0f, 0f))
                }
                lightNodes.values.forEach {
                    immediateTechnique.marker(camera, it.calculateM(), it.payload().intensity)
                }
            }
        }
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
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
