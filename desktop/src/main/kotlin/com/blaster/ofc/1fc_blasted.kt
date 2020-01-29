package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.*
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
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.regex.Pattern

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val deferredTechnique = DeferredTechnique()
private val immediateTechnique = ImmediateTechnique()
private val textTechnique = TextTechnique()
private val skyboxTechnique = SkyboxTechnique()

private val console = Console(1000L)

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.3f)
private val wasd = WasdInput(controller)

private lateinit var baseModel: Model

private val nodes = mutableMapOf<String, Node<Model>>()

private var lastUpdate = 0L
private var currentScene = listOf<Marker>()

private var mouseControl = false

// todo: materials, models, particles, lights and camera

class SceneDiffer {
    open class Listener {
        open fun onRemove(marker: Marker) {}
        open fun onAdd(marker: Marker) {}
        open fun onUpdate(marker: Marker) {}
        open fun onParent(marker: Marker, parent: Marker?) {}
    }

    fun diff(prevMarkers: List<Marker> = listOf(), nextMarkers: List<Marker>, listener: Listener) {
        diffInternal(prevMarkers, nextMarkers, listener)
    }

    private data class ParentToChild(val parent: Marker?, val child: Marker)

    private fun enumerate(parent: Marker?, markers: List<Marker>, parentToChild: MutableList<ParentToChild>) {
        markers.forEach {
            parentToChild.add(ParentToChild(parent, it))
            enumerate(it, it.children, parentToChild)
        }
    }

    private fun diffInternal(prevMarkers: List<Marker>, nextMarkers: List<Marker>, listener: Listener) {
        val parentToChildPrev = mutableListOf<ParentToChild>()
        enumerate(null, prevMarkers, parentToChildPrev)
        val parentToChildNext = mutableListOf<ParentToChild>()
        enumerate(null, nextMarkers, parentToChildNext)
        parentToChildPrev.forEach { prev ->
            val found = parentToChildNext.firstOrNull { prev.child.uid == it.child.uid }
            if (found == null) {
                listener.onRemove(prev.child)
            } else {
                if (found.child != prev.child) {
                    listener.onUpdate(found.child)
                }
                if (found.parent != prev.parent) {
                    listener.onParent(found.child, found.parent)
                }
            }
        }
        parentToChildNext.forEach { next ->
            val found = parentToChildPrev.firstOrNull { next.child.uid == it.child.uid }
            if (found == null) {
                listener.onAdd(next.child)
                listener.onParent(next.child, next.parent)
            }
        }
    }
}

// todo: templates by id
// todo: toLeftOf, toRightOf, toTopOf, toBottomOf, toFrontOf, toBackOf - by aabb (which is always axis aligned)
// todo: probably, also can have matrix directly?
// todo: target as a name
// todo: the rest of the string is custom stuff

private const val START_POS = "pos "
private const val START_QUAT = "quat "
private const val START_EULER = "euler "
private const val START_SCALE = "scale "
private const val START_BOUND = "bound "
private const val START_DIR = "dir "
private const val START_TARGET = "target "
private const val START_CUSTOM = "custom "

class SceneReader {
    fun load(sceneStream: InputStream): List<Marker> {
        val lines = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
                .readLines().toMutableList()
        return parse(0, lines)
    }

    private fun peek(input: String): Int {
        var count = 0
        while (input[count] == ' ' || input[count] == '\t') {
            count++
        }
        return count
    }

    private fun parse(depth: Int, remainder: MutableList<String>): List<Marker> {
        val uids = hashSetOf<String>()
        val result = mutableListOf<Marker>()
        loop@ while (remainder.isNotEmpty()) {
            if (remainder[0].isBlank() || remainder[0].trim().startsWith("//")) {
                remainder.removeAt(0)
                continue
            }
            val currentDepth = peek(remainder[0])
            when {
                currentDepth == depth -> result.add(parseMarker(remainder.removeAt(0), uids))
                currentDepth > depth -> result.last().children.addAll(parse(currentDepth, remainder))
                currentDepth < depth -> break@loop
            }
        }
        return result
    }

    private fun parseMarker(marker: String, uids: MutableSet<String>): Marker {
        val tokens = marker.trim().split(Pattern.compile(";")).dropLast(1)
        val uid: String = tokens[0]
        check(uids.add(uid)) { "Non unique uid: $uid" }
        var pos: vec3? = null
        var quat: quat? = null
        var euler: euler3? = null
        var scale: vec3? = null
        var bound: Float? = null
        var dir: vec3? = null
        var target: vec3? = null
        var custom: String? = null
        tokens.forEachIndexed { index, token ->
            val trimmed = token.trim()
            when {
                trimmed.startsWith(START_POS)    -> pos      = trimmed.removePrefix(START_POS).toVec3()
                trimmed.startsWith(START_QUAT)   -> quat     = trimmed.removePrefix(START_QUAT).toQuat()
                trimmed.startsWith(START_EULER)  -> euler    = trimmed.removePrefix(START_EULER).toVec3()
                trimmed.startsWith(START_SCALE)  -> scale    = trimmed.removePrefix(START_SCALE).toVec3()
                trimmed.startsWith(START_BOUND)  -> bound    = trimmed.removePrefix(START_BOUND).toFloat()
                trimmed.startsWith(START_DIR)    -> dir      = trimmed.removePrefix(START_DIR).toVec3()
                trimmed.startsWith(START_TARGET) -> target   = trimmed.removePrefix(START_TARGET).toVec3()
                trimmed.startsWith(START_CUSTOM) -> custom   = trimmed.removePrefix(START_CUSTOM)
                else -> if (index != 0) { fail("Unhandled: $token") }
            }
        }
        return Marker(uid, pos ?: vec3(), euler, quat, scale, bound, dir, target, custom, mutableListOf())
    }
}

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

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

private val window = object : LwjglWindow(isHoldingCursor = false) {
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
        skyboxTechnique.prepare(shadersLib, texturesLib, meshLib, "textures/sincity")
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
            console.success("Scene reloaded..")
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
        nodes.values.forEach {
            immediateTechnique.aabb(camera, it.payload!!.aabb, it.calculateM(), color(1f, 0f, 0f))
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
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}
