package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.Console
import com.blaster.gl.GlLocator
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.gl.glCheck
import com.blaster.platform.LwjglWindow
import com.blaster.scene.*
import com.blaster.techniques.BillboardsTechnique
import com.blaster.techniques.TextTechnique
import org.joml.AABBf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.sin

private const val W = 800
private const val H = 600

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

private lateinit var snowflakeDiffuse: GlTexture
private lateinit var flameDiffuse: GlTexture

private val sceneAABB = AABBf(Vector3f(-5f), Vector3f(5f))

const val BILLB_MAX = 1000

const val SNOWFLAKE_SIDE = 0.1f
const val FLAMES_SIDE = 1.5f

private val random = Random()

private val immediateTechnique = ImmediateTechnique()
private val billboardsTechnique = BillboardsTechnique(BILLB_MAX)
private val textTechnique = TextTechnique()

private val snow = Particles(BILLB_MAX, snowflakeEmitters(), ::emitSnowflake, ::updateSnowflake)
private val flame = Particles(BILLB_MAX, listOf(Vector3f(0f, -1f, 0f)), ::emitFlame, ::updateFlame)

private val console = Console(1000L)

private val camera = Camera(W.toFloat() / H.toFloat())
private val controller = Controller()
private val node = Node()

private val backend = GlLocator.locate()

class ImmediateTechnique {
    private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())

    fun prepare(camera: Camera) {
        glCheck {
            backend.glMatrixMode(backend.GL_PROJECTION)
            camera.projectionM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
        }
    }

    fun aabb(camera: Camera, aabb: AABBf, color: Vector3f = Vector3f(1f)) {
        // todo: glFrustum, glLoadIdentity, glLoadMatrix, glLoadTransposeMatrix, glMatrixMode, glMultMatrix, glMultTransposeMatrix, glOrtho, glRotate, glScale, glTranslate, glViewport
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            camera.calculateViewM().get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)

            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.minZ)

            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glEnd()
        }
    }
}

class Snowflake(origin: Vector3f) : Particle(origin) {
    val origin = Vector3f(origin)
    val randomness = random.nextFloat()
}

private fun snowflakeEmitters(): List<Vector3f> {
    val emitters = mutableListOf<Vector3f>()
    for (x in -5..5) {
        for (z in -5..5) {
            emitters.add(Vector3f(x.toFloat(), 2f, z.toFloat()))
        }
    }
    return emitters
}

private fun emitSnowflake(emitter: Vector3f, particles: MutableList<Particle>) {
    if (random.nextInt(50) == 1) {
        particles.add(Snowflake(emitter))
        console.info("Snowflake ${particles.size}")
    }
}

private fun updateSnowflake(particle: Particle): Boolean {
    val snowflake = particle as Snowflake
    snowflake.position.y -= 0.01f
    snowflake.position.x = snowflake.origin.x + sin((snowflake.randomness + snowflake.position.y) * 2f) * 0.5f
    snowflake.position.z = snowflake.origin.z + sin((snowflake.randomness + snowflake.position.y) * 4f) * 0.2f
    return particle.position.y > -2f
}

class Flame(origin: Vector3f) : Particle(origin)

private fun emitFlame(emitter: Vector3f, particles: MutableList<Particle>) {
    if (random.nextInt(5) == 1) {
        particles.add(Flame(emitter))
        console.success("Flame ${particles.size}")
    }
}

private fun updateFlame(particle: Particle): Boolean {
    val flame = particle as Flame
    flame.position.y += 0.01f
    flame.position.x += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    flame.position.z += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    return particle.position.y < -0.5f
}

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        controller.position.set(Vector3f(0f, 0f, 3f))
        console.info("Controller set..")
        billboardsTechnique.prepare(shadersLib)
        console.info("Particles ready..")
        textTechnique.prepare(shadersLib, texturesLib)
        immediateTechnique.prepare(camera)
        console.info("Techniques ready..")
        snowflakeDiffuse = texturesLib.loadTexture("textures/snowflake.png")
        flameDiffuse = texturesLib.loadTexture("textures/flame.png")
        console.info("Textures loaded..")
        GlState.apply(color = Vector3f())
        console.success("All ready..")
    }

    override fun onDraw() {
        GlState.clear()
        snow.tick()
        flame.tick()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        console.tick()
        immediateTechnique.aabb(camera, sceneAABB)
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
            }
        }
        billboardsTechnique.draw(camera) {
            billboardsTechnique.instance(snow, node, snowflakeDiffuse, 1.0f, SNOWFLAKE_SIDE, SNOWFLAKE_SIDE)
            GlState.drawTransparent {
                billboardsTechnique.instance(flame, node, flameDiffuse, 0.4f, FLAMES_SIDE, FLAMES_SIDE)
            }
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        controller.yaw(delta.x)
        controller.pitch(-delta.y)
    }

    override fun keyPressed(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.w = true
            GLFW.GLFW_KEY_A -> controller.a = true
            GLFW.GLFW_KEY_S -> controller.s = true
            GLFW.GLFW_KEY_D -> controller.d = true
            GLFW.GLFW_KEY_E -> controller.e = true
            GLFW.GLFW_KEY_Q -> controller.q = true
        }
    }

    override fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.w = false
            GLFW.GLFW_KEY_A -> controller.a = false
            GLFW.GLFW_KEY_S -> controller.s = false
            GLFW.GLFW_KEY_D -> controller.d = false
            GLFW.GLFW_KEY_E -> controller.e = false
            GLFW.GLFW_KEY_Q -> controller.q = false
        }
    }
}

fun main() {
    window.show()
}