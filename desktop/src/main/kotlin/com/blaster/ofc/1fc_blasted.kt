package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ModelsLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.vec2
import com.blaster.common.vec3
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
import org.joml.Vector2f

private val scene = """
    building0; pos 0;
    building2; pos 44 0 0; bound 1; euler -90 0 0;
    building3; pos 55 0 0; bound 2; euler 0 -90 0;
    building4; pos 66 0 0; bound 3; euler 0 0 -90;
""".trimIndent()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

private val deferredTechnique = DeferredTechnique()

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private lateinit var baseModel: Model

private val nodes = mutableMapOf<String, Node<Model>>()

private val listener = object : SceneDiffer.Listener() {
    override fun onAdd(marker: Marker) {
        val node = Node(payload = baseModel)
        marker.apply(node)
        nodes[marker.uid] = node
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        if (parent != null) {
            nodes[parent.uid]!!.attach(nodes[marker.uid]!!)
        }
    }
}

class BlastEd {
    // todo: WYSIWYG, throttling, error handling

    // Caches aren independent from BlastEd
    // Template libraries are independent from BlastEd
    // Rendering is independent from BlastEd

    // 1 - throttle
    // 2 - read from template
    // 3 - update scene
}

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        controller.position.set(vec3(0.5f, 3f, 3f))
        baseModel = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        deferredTechnique.prepare(shadersLib, width, height)
        camera = Camera(width.toFloat() / height.toFloat())
        sceneDiffer.diff(nextMarkers = sceneReader.load(scene), listener = listener)
        deferredTechnique.light(Light.SUNLIGHT)
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        deferredTechnique.draw(camera) {
            for (node in nodes.values) {
                val model = node.payload!!
                deferredTechnique.instance(model.mesh, node.calculateM(), model.diffuse, Material.CONCRETE)
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
