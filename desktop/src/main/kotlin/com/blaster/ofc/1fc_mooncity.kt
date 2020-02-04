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

const val HEIGHT = 50f
const val SIDE = 25f // 25m each building side
const val COUNT = 50 // 50 buildings in a row/column

// todo: flat terrain, aabb grid, endless repeating of grid
// todo: level of details for meshes from split grammars
// todo: animated details with billboards
// todo: no roads: everything is flying in echelons
// todo: graph of navigation for vehicles/pedestrians

private val SPLIT_LINE = Pattern.compile(":\\s+")
private val SPLIT_RULES = Pattern.compile("\\s+")

private enum class GrammarNodeCnt {
    ONE,            // 1
    NONE_OR_ONE,    // ?
    NONE_OR_MORE,   // *
    ONE_OR_MORE,    // +
}

private data class GrammarNode(
        val label: String,
        val splitRule: SplitRule,
        val children: List<Pair<GrammarNode, GrammarNodeCnt>>?)

typealias SplitRule = (aabb: aabb) -> List<aabb>

class Grammar private constructor() {

    private var root: GrammarNode? = null

    fun walk() {

    }

    companion object {
        fun compile(grammar: String, splitRules: Map<String, SplitRule>): Grammar
        {
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
            result.root = parseNode(rootLabel!!, parsed, splitRules)
            return result
        }

        private fun parseNode(label: String,
                              parsed: Map<String, List<String>>,
                              splitRules: Map<String, SplitRule>): GrammarNode {
            if (label.filter { !it.isUpperCase() }.count() == 0) {
                return GrammarNode(label, splitRules.getValue(label), null) // terminal node
            }
            val rules = parsed.getValue(label)
            val children = mutableListOf<Pair<GrammarNode, GrammarNodeCnt>>()
            rules.forEach {
                val (child, cnt) = when {
                    it.endsWith("?") -> it.removeSuffix("?") to GrammarNodeCnt.NONE_OR_ONE
                    it.endsWith("*") -> it.removeSuffix("*") to GrammarNodeCnt.NONE_OR_MORE
                    it.endsWith("+") -> it.removeSuffix("+") to GrammarNodeCnt.ONE_OR_MORE
                    else -> it to GrammarNodeCnt.ONE
                }

                children.add(parseNode(child, parsed, splitRules) to cnt)
            }
            return GrammarNode(label, splitRules.getValue(label), children)
        }
    }
}

private val grammar = Grammar.compile(
        """
        mooncity:   block+
        block:      building+
        building:   base roof floor+ 
        base:       SHAPE
        floor:      SHAPE
        roof:       SHAPE
    """,
        mapOf(
                "mooncity"  to ::mooncity,
                "block"     to ::block,
                "building"  to ::building,
                "base"      to ::base,
                "roof"      to ::roof,
                "floor"     to ::floor,
                "SHAPE"     to ::shape
        ))

fun mooncity(aabb: aabb) = aabb.randomSplit(SIDE, listOf(0, 2))

fun block(aabb: aabb): List<aabb> {
    TODO()
}

fun building(aabb: aabb): List<aabb> {
    TODO()
}

fun base(aabb: aabb): List<aabb> {
    TODO()
}

fun roof(aabb: aabb): List<aabb> {
    TODO()
}

fun floor(aabb: aabb): List<aabb> {
    TODO()
}

fun shape(aabb: aabb): List<aabb> {
    TODO()
}

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