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
import java.lang.IllegalArgumentException

// todo: aabb with immediate technique

private val scene = """
    building0; pos 0 0 0; bound 20;
    building1; pos 10 0 0; bound 20;
    building2; pos 20 0 0; bound 20;
    building3; pos 0 0 10; bound 20; euler 0 -180 0;
    building4; pos 10 0 10; bound 20; euler 0 -180 0;
    building5; pos 20 0 10; bound 20; euler 0 -180 0;
""".trimIndent()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val modelsLib = MeshLib(assetStream, texturesLib)

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

private val deferredTechnique = DeferredTechnique()

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private lateinit var baseModel: Model

private val nodes = mutableMapOf<String, Node<Model>>()

private val sceneListener = object : SceneDiffer.Listener() {
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

sealed class Template

const val START_MATERIAL = "material "
const val START_MODEL = "model "
const val START_PARTICLES = "particles "
const val START_LIGHT = "light "

data class MaterialTemplate(
        val uid: String,
        val ambient: vec3, val diffuse: vec3, val specular: vec3,
        val shine: Float, val transparency: Float) : Template() {

    companion object {
        fun parse(uid: String, iterator: Iterator<String>) =
                MaterialTemplate(uid,
                        iterator.next().dropBeforeDigit().toVec3(), iterator.next().dropBeforeDigit().toVec3(), iterator.next().dropBeforeDigit().toVec3(),
                        iterator.next().dropBeforeDigit().toFloat(), iterator.next().dropBeforeDigit().toFloat())
    }
}

//  todo: material

data class ModelTemplate(val uid: String, val obj: String, val diffuse: String) : Template() {
    companion object {
        fun parse(uid: String, iterator: Iterator<String>)
                = ModelTemplate(uid, iterator.next().trim().removePrefix("obj "), iterator.next().trim().removePrefix("diffuse "))
    }
}

class ParticlesTemplate : Template()
class LightTemplate : Template()

private val TEMPLATE_EXAMPLE = """
    material Metal
        ambient 1 1 1
        diffuse 1 1 1
        specular 1 1 1
        shine 1
        transparency 1
        
    material Glass
        ambient 1 1 1
        diffuse 1 1 1
        specular 1 1 1
        shine 1 
        transparency 1
        
    model House
        obj models/house/low.obj
        diffuse models/house/house_diffuse.png
""".trimIndent()

class TemplateReader {
    fun load(string: String) : List<Template> {
        val result = mutableListOf<Template>()
        val lines = string.lines()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (line.isBlank()) {
                continue
            }
            result.add(when {
                line.startsWith(START_MATERIAL) -> MaterialTemplate.parse(line.removePrefix(START_MATERIAL), iterator)
                line.startsWith(START_MODEL) -> ModelTemplate.parse(line.removePrefix(START_MODEL), iterator)
                else -> throw IllegalArgumentException("Wtf?! $line")
            })
        }
        return result
    }
}

class TemplateDiffer {
    fun diff(prev: List<Template> = listOf(), next: List<Template>, listener: Listener) {

    }

    open class Listener {
        fun onRemoved(template: Template) {}
        fun onUpdated(template: Template) {}
        fun onAdded(template: Template) {}
    }
}

private val templateReader = TemplateReader()
private val templateDiffer = TemplateDiffer()

private val templateListener = object : TemplateDiffer.Listener() {

}

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        controller.position.set(vec3(0.5f, 3f, 3f))
        baseModel = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        deferredTechnique.prepare(shadersLib, width, height)
        camera = Camera(width.toFloat() / height.toFloat())
        sceneDiffer.diff(nextMarkers = sceneReader.load(scene), listener = sceneListener)
        deferredTechnique.light(Light.SUNLIGHT)


        val templates = templateReader.load(TEMPLATE_EXAMPLE)
        templateDiffer.diff(next = templates, listener = templateListener)
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
