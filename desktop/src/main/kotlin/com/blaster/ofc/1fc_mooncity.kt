package com.blaster.ofc

import com.blaster.common.*
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.techniques.ImmediateTechnique
import org.lwjgl.glfw.GLFW
import java.util.regex.Pattern

// todo: flat terrain, aabb grid
// todo: level of details for meshes from simple rules (l-systems)
// todo: animated details with billboards
// todo: no roads: everything is flying in echelons
// todo: graph of navigation for vehicles/pedestrians
// todo: endless to traverse by repeating the grid

private val SPLIT_LINE = Pattern.compile(":\\s+")
private val SPLIT_RULES = Pattern.compile("\\s+")

private enum class GrammarNodeCnt {
    ONE,            // 1
    NONE_OR_ONE,    // ?
    NONE_OR_MORE,   // *
    ONE_OR_MORE,    // +
}

private data class GrammarNode(val label: String, val cnt: GrammarNodeCnt, val children: List<GrammarNode>?)

class Grammar private constructor() {

    private var root: GrammarNode? = null

    fun walk(terminals: Map<String, () -> Unit>) {

    }

    companion object {
        fun compile(grammar: String): Grammar {
            var rootLabel: String? = null
            val parsed = mutableMapOf<String, List<String>>()
            grammar.lines().forEach {
                if (!it.isBlank()) {
                    val trimmed = it.trim()
                    val split = trimmed.split(SPLIT_LINE)
                    val label = split[0]
                    val rules = split[1]
                    if (rootLabel == null) {
                        rootLabel = label
                    }
                    val rulesSplit = rules.split(SPLIT_RULES)
                    check(!parsed.contains(label))
                    parsed[label] = rulesSplit
                }
            }
            val result = Grammar()
            result.root = parseNode(rootLabel!!, parsed)
            return result
        }

        private fun parseNode(rule: String, parsed: Map<String, List<String>>): GrammarNode {
            val (label, cnt) = when {
                rule.endsWith("?") -> rule.removeSuffix("?") to GrammarNodeCnt.NONE_OR_ONE
                rule.endsWith("*") -> rule.removeSuffix("*") to GrammarNodeCnt.NONE_OR_MORE
                rule.endsWith("+") -> rule.removeSuffix("+") to GrammarNodeCnt.ONE_OR_MORE
                else -> rule to GrammarNodeCnt.ONE
            }
            if (label.filter { !it.isUpperCase() }.count() == 0) {
                return GrammarNode(label, cnt, null) // terminal node
            }
            val rules = parsed.getValue(label)
            val children = mutableListOf<GrammarNode>()
            rules.forEach { children.add(parseNode(it, parsed)) }
            return GrammarNode(label, cnt, children)
        }
    }
}

// each rule takes a rectangular piece of volume from the rest of the aabb
// if there is not enough space left - terminated

// section: split-xz non-intersecting
// building: split-xz intersecting
// base, floor, roof - split-y non-intersecting

private val grammar = Grammar.compile(
    """
        mooncity: section+
        section: pavilion+
        pavilion: base floor+ roof
        base: SHAPE
        floor: SHAPE
        roof: SHAPE
    """
)

fun aabb.randomSplit(min: Float, axises: List<Int> = listOf(0, 1, 2)): List<aabb> {
    val axisesCopy = ArrayList(axises)
    while(axisesCopy.isNotEmpty()) {
        val randomAxis = axisesCopy.random()
        val split = randomSplitByAxis(randomAxis, min)
        if (split != null) {
            return split.flatMap { it.randomSplit(min, axises) }
        } else {
            axisesCopy.remove(randomAxis)
        }
    }
    return listOf(this) // terminal
}

fun aabb.randomSplitByAxis(axis: Int, min: Float): List<aabb>? {
    val (from, to) = when (axis) {
        0 -> minX to maxX
        1 -> minY to maxY
        2 -> minZ to maxZ
        else -> throw IllegalArgumentException("wtf?!")
    }
    check(to > from)
    val space = to - from
    if (min * 2f > space) {
        return null
    }
    val randFrom = from + min
    val randTo = to - min
    val selected = randomf(randFrom, randTo)
    return when (axis) {
        0 -> listOf(aabb(from, minY, minZ, selected, maxY, maxZ), aabb(selected, minY, minZ, to, maxY, maxZ))
        1 -> listOf(aabb(minX, from, minZ, maxX, selected, maxZ), aabb(minX, selected, minZ, maxX, to, maxZ))
        2 -> listOf(aabb(minX, minY, from, maxX, maxY, selected), aabb(minX, minY, selected, maxX, maxY, to))
        else -> throw IllegalArgumentException("wtf?!")
    }
}

fun section(bounds: aabb) {
    // has own constants/parameters
    // returns aabb, which it took or null if terminated
}

const val HEIGHT = 50f
const val SIDE = 25f // 25m each building side
const val COUNT = 50 // 50 buildings in a row/column

private val immediateTechnique = ImmediateTechnique()

private val camera = Camera()
private val controller = Controller(position = vec3(0f, HEIGHT * 2f, 0f), yaw = radf(45f), velocity = 1f)
private val wasd = WasdInput(controller)
private var mouseControl = false

/*private val aabb = aabb(-SIDE, 0f, -SIDE, SIDE, HEIGHT, SIDE)

data class Section(val node: Node<Any>)

class Grid {
    val sections: List<Section>

    init {
        val result = mutableListOf<Section>()
        for (row in 0 until COUNT) {
            for (column in 0 until COUNT) {
                val pos = vec3(row * SIDE, 0f, column * SIDE)
                val node = Node<Any>()
                node.setPosition(pos)
                result.add(Section(node))
            }
        }
        sections = result
    }

    fun get(row: Int, column: Int): Section {
        check(row in 0 until COUNT)
        check(column in 0 until COUNT)
        return sections[row + column * COUNT]
    }
}

private val grid = Grid()*/

private val aabbs = aabb(-30f, 0f, -30f, 30f, 30f, 30f).randomSplit(10f, axises = listOf(0, 2))

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {

    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height, color = color(0f))
        camera.setPerspective(width, height)
        immediateTechnique.resize(camera)
    }

    private fun tick() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    private fun draw() {
        GlState.clear()
        val identity = mat4()
        aabbs.forEach { immediateTechnique.aabb(camera, it, identity, color = color(0f, 1f, 0f)) }
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