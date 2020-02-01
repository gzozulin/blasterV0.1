package com.blaster.ofc

import com.blaster.common.*
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.scene.Node
import com.blaster.techniques.ImmediateTechnique
import org.lwjgl.glfw.GLFW

// todo: flat terrain, aabb grid
// todo: level of details for meshes from simple rules (l-systems)
// todo: animated details with billboards
// todo: no roads: everything is flying in echelons
// todo: graph of navigation for vehicles/pedestrians

private val immediateTechnique = ImmediateTechnique()

private val camera = Camera()
private val controller = Controller(position = vec3(0f, 25f, 0f), yaw = radf(45f), velocity = 1f)
private val wasd = WasdInput(controller)
private var mouseControl = false

// aabb -> trunks -> stories -> panels -> textures, materials, vertices, normals, tex coords, indices

const val SIDE = 25f // 25m each building side
const val COUNT = 50 // 50 buildings in a row/column

private val aabb = aabb(-SIDE, -10f, -SIDE, SIDE, 10f, SIDE)

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

private val grid = Grid()

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
        grid.sections.forEach {
            immediateTechnique.aabb(camera, aabb, it.node.calculateM(), color = color(0f, 1f, 0f))
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