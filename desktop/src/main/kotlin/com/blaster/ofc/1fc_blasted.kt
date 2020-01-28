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
import com.blaster.scene.*
import com.blaster.techniques.DeferredTechnique
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.TextTechnique
import java.io.File
import java.lang.Exception

// todo: aabb with immediate technique

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

private val deferredTechnique = DeferredTechnique()
private val immediateTechnique = ImmediateTechnique()
private val textTechnique = TextTechnique()

private val console = Console(2000L)

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private lateinit var baseModel: Model

private val nodes = mutableMapOf<String, Node<Model>>()

private var lastUpdate = 0L
private var currentScene = listOf<Marker>()

private val sceneListener = object : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        val node = nodes[marker.uid]!!
        node.detachFromParent()
        nodes.remove(marker.uid)
        console.info("Marker removed: ${marker.uid}")
    }

    override fun onAdd(marker: Marker) {
        val node = Node(payload = baseModel)
        marker.apply(node)
        nodes[marker.uid] = node
        console.info("Marker added: ${marker.uid}")
    }

    override fun onUpdate(marker: Marker) {
        val node = nodes[marker.uid]!!
        marker.apply(node)
        console.info("Marker updated: ${marker.uid}")
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        if (parent != null) {
            nodes[parent.uid]!!.attach(nodes[marker.uid]!!)
        }
        console.info("Marker ${marker.uid} attached to ${parent?.uid}")
    }
}

private val window = object : LwjglWindow(isHoldingCursor = true) {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        controller.position.set(vec3(0.5f, 3f, 3f))
        val (mesh, aabb) = meshLib.loadMesh("models/house/low.obj")
        val diffuse = texturesLib.loadTexture("models/house/house_diffuse.png")
        baseModel = Model(mesh, diffuse, aabb, Material.CONCRETE)
        deferredTechnique.prepare(shadersLib, width, height)
        camera = Camera(width.toFloat() / height.toFloat())
        deferredTechnique.light(Light.SUNLIGHT)
        immediateTechnique.prepare(camera)
        textTechnique.prepare(shadersLib, texturesLib)
    }

    private fun tickScene() {
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
            console.success("Scene reloaded..")
        }
    }

    override fun onTick() {
        tickScene()
        console.tick()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
            }
        }
        nodes.values.forEach {
            immediateTechnique.aabb(camera, it.payload!!.aabb, it.calculateM(), color(1f, 0f, 0f))
        }
        deferredTechnique.draw(camera) {
            for (node in nodes.values) {
                val model = node.payload!!
                deferredTechnique.instance(model.mesh, node.calculateM(), model.diffuse, model.material)
            }
        }
    }

    override fun onCursorDelta(delta: vec2) {
        wasd.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}
