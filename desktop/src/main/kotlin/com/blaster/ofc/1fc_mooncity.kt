package com.blaster.ofc

import com.blaster.common.*
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.techniques.ImmediateTechnique
import org.lwjgl.glfw.GLFW

// todo: flat terrain, aabb grid, endless repeating of grid
// todo: level of details for meshes from split grammars
// todo: animated details with billboards
// todo: no roads: everything is flying in echelons
// todo: graph of navigation for vehicles/pedestrians

const val HEIGHT = 100f
const val BLOCK_SIDE = 25f // 25m each block
const val CITY_SIDE = BLOCK_SIDE * 25

private val mooncityAabb = aabb(vec3(-CITY_SIDE, 0f, -CITY_SIDE), vec3(CITY_SIDE, HEIGHT, CITY_SIDE))
private val shapes = mutableListOf<aabb>()

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

fun mooncity(aabb: aabb) = aabb.randomSplit(listOf(0, 2), BLOCK_SIDE)
fun block(aabb: aabb) = aabb.selectCentersInside(randomi(1, 5), 15f, BLOCK_SIDE)
        .map { it.splitByAxis(1, listOf(randomf(0.7f, 1.0f))).first() }

fun building(aabb: aabb): List<aabb> {
    val split = aabb.splitByAxis(1, listOf(0.1f, 0.8f, 0.1f))
    return listOf(split[0], split[2], split[1])
}

fun base(aabb: aabb) = listOf(aabb)
fun roof(aabb: aabb) = listOf(aabb)
fun floor(aabb: aabb) = listOf(aabb)

fun shape(aabb: aabb): List<aabb> {
    shapes.add(aabb)
    return emptyList()
}

fun aabb.randomSplit(axises: List<Int> = listOf(0, 1, 2), min: Float): List<aabb> {
    val axisesCopy = ArrayList(axises)
    while(axisesCopy.isNotEmpty()) {
        val axis = axisesCopy.random()
        val length = when (axis) {
            0 -> width()
            1 -> height()
            2 -> depth()
            else -> throw IllegalStateException("wtf?!")
        }
        val from  = 0.3f
        val to = 0.7f
        val minLength = length * 0.3f
        if (minLength > min) {
            val first = randomf(from, to)
            val second = 1f - first
            return splitByAxis(axis, listOf(first, second))
                    .flatMap { it.randomSplit(axises, min) }
        } else {
            axisesCopy.remove(axis)
        }
    }
    return listOf(this) // terminal
}

fun aabb.splitByAxis(axis: Int, ratios: List<Float>): List<aabb> {
    val result = mutableListOf<aabb>()
    val (from, to) = when (axis) {
        0 -> minX to maxX
        1 -> minY to maxY
        2 -> minZ to maxZ
        else -> throw IllegalArgumentException("wtf?!")
    }
    check(to > from)
    val length = to - from
    var start = from
    ratios.forEach { ratio ->
        val end = start + length * ratio
        result.add(when (axis) {
            0 -> aabb(start, minY, minZ, end, maxY, maxZ)
            1 -> aabb(minX, start, minZ, maxX, end, maxZ)
            2 -> aabb(minX, minY, start, maxX, maxY, end)
            else -> throw IllegalArgumentException("wtf?!")
        })
        start = end
    }
    return result
}

fun aabb.selectCentersInside(cnt: Int, minR: Float, maxR: Float): List<aabb> {
    check(cnt > 0 && maxR > minR)
    val result = mutableListOf<aabb>()
    while (result.size != cnt) {
        val r = randomf(minR, maxR)
        val fromX = minX + r
        val toX = maxX - r
        val fromZ = minZ + r
        val toZ = maxZ - r
        val x = randomf(fromX, toX)
        val z = randomf(fromZ, toZ)
        result.add(aabb(x - r, minY, z - r, x + r, maxY, z + r))
    }
    return result
}

private val immediateTechnique = ImmediateTechnique()

private val camera = Camera()
private val controller = Controller(position = vec3(0f, HEIGHT * 2f, 0f), yaw = radf(45f), velocity = 5f)
private val wasd = WasdInput(controller)
private var mouseControl = false

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        grammar.walk(mooncityAabb)
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
        shapes.forEach { immediateTechnique.aabb(camera, it, identity, color = color(0f, 1f, 0f)) }
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