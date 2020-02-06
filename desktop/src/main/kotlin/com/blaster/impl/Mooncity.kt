package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.aux.*
import com.blaster.entity.Light
import com.blaster.entity.Material
import com.blaster.entity.Model
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.tools.Grammar
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.entity.Camera
import com.blaster.entity.Controller
import com.blaster.tools.Mesh
import com.blaster.entity.Node
import com.blaster.techniques.DeferredTechnique
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.SkyboxTechnique
import org.lwjgl.glfw.GLFW

const val HEIGHT = 100f
const val BLOCK_SIDE = 25f // 25m each block
const val CITY_SIDE = BLOCK_SIDE * 25

private val mooncityAabb = aabb(vec3(-CITY_SIDE, 0f, -CITY_SIDE), vec3(CITY_SIDE, HEIGHT, CITY_SIDE))

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val meshLib = MeshLib(assetStream)
private val textureLib = TexturesLib(assetStream)

private val immediateTechnique = ImmediateTechnique()
private val deferredTechnique = DeferredTechnique()
private val skyboxTechnique = SkyboxTechnique()

private val camera = Camera()
private val controller = Controller(position = vec3(0f, HEIGHT * 2f, 0f), yaw = radf(45f), velocity = 5f)
private val wasd = WasdInput(controller)
private var mouseControl = false

private val daylight = Light(vec3(1f), false)
private val daylightNode = Node(payload = daylight).lookAlong(vec3(-1f, -0.7f, -0.3f))

private lateinit var cube: Mesh
private lateinit var cubeAabb: aabb
private val meganode = Node<Model>()
private val cubeNodes = mutableListOf<Node<Model>>()
private lateinit var cubeTexture: GlTexture
private lateinit var earthNode: Node<Model>

private val grammar = Grammar.compile(
    """
        mooncity:   block+
        block:      building+
        building:   SHAPE SHAPE SHAPE+
    """,
        mapOf(
                "mooncity"  to ::mooncity,
                "block"     to ::block,
                "building"  to ::building,
                "SHAPE"     to ::shape
        ))

fun mooncity(aabb: aabb) = aabb.randomSplit(listOf(0, 2), BLOCK_SIDE)
fun block(aabb: aabb) = aabb.selectCentersInside(randi(1, 3), 10f, 20f)
        .map { it.splitByAxis(1, listOf(randf(0.7f, 1.0f))).first() }

fun building(aabb: aabb): List<aabb> {
    val split = aabb.splitByAxis(1, listOf(0.1f, 0.8f, 0.1f))
    return listOf(split[0], split[2], split[1])
}

fun shape(aabb: aabb): List<aabb> {
    val node = Node(parent = meganode, payload = Model(cube, cubeTexture, aabb, Material.MATERIALS.values.random()))
    node.setPosition(aabb.center())
    node.setScale(cubeAabb.scaleTo(aabb))
    cubeNodes.add(node)
    return emptyList()
}

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        skyboxTechnique.create(shadersLib, textureLib, meshLib, skybox = "textures/nuke")
        deferredTechnique.create(shadersLib)
        cubeTexture = textureLib.loadTexture("textures/marble.jpeg")
        val (mesh, aabb) = meshLib.loadMesh("models/cube/cube.obj")
        cube = mesh
        cubeAabb = aabb
        earthNode = Node(meganode, payload = Model(cube, cubeTexture, cubeAabb, Material.SILVER))
        earthNode.setScale(vec3(CITY_SIDE, 0.1f, CITY_SIDE))
        grammar.walk(mooncityAabb)
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height, color = color(0f))
        camera.setPerspective(width, height)
        deferredTechnique.resize(width, height)
        immediateTechnique.resize(camera)
    }

    private fun tick() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        meganode.rotate(VECTOR_UP, 0.001f)
        cubeNodes.sortBy { it.position.distanceSquared(camera.position) }
    }

    private fun draw() {
        GlState.clear()
        deferredTechnique.draw(camera, meshes =  {
            cubeNodes.forEach {
                val model = it.payload()
                deferredTechnique.instance(model.mesh, it.calculateM(), model.diffuse, model.material)
            }
            val payload = earthNode.payload()
            deferredTechnique.instance(payload.mesh, earthNode.calculateM(), payload.diffuse, payload.material)
        }, lights = {
            deferredTechnique.light(daylight, daylightNode.calculateM())
        })
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