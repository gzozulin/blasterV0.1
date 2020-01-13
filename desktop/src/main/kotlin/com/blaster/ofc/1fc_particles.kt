package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.AABB
import com.blaster.common.Console
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.scene.*
import com.blaster.techniques.BillboardsTechnique
import com.blaster.techniques.TextTechnique
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.math.sin

private const val W = 800
private const val H = 600

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

private lateinit var snowflakeDiffuse: GlTexture

private val sceneAABB = AABB(Vector3f(-5f), Vector3f(5f))

const val BILLBOARDS_MAX = 1000
const val BILLBOARDS_SIDE = 0.1f

private val random = Random()

private val immediateTechnique = ImmediateTechnique()
private val billboardsTechnique = BillboardsTechnique(BILLBOARDS_MAX)
private val textTechnique = TextTechnique()

private val particles = Particles(BILLBOARDS_MAX, snowflakeEmitters(), ::emitSnowflake, ::updateSnowflake)

private val console = Console(1000L)

private val camera = Camera(W.toFloat() / H.toFloat())
private val controller = Controller()
private val node = Node()

class ImmediateTechnique {
    fun aabb(camera: Camera, aabb: AABB, color: Vector3f = Vector3f(1f)) {
        // todo: glFrustum, glLoadIdentity, glLoadMatrix, glLoadTransposeMatrix, glMatrixMode, glMultMatrix, glMultTransposeMatrix, glOrtho, glRotate, glScale, glTranslate, glViewport
        /*glCheck {
            backend.glBegin(backend.GL_LINES)
            //backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.min.x, aabb.min.y, aabb.min.z)
            //backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.max.x, aabb.max.y, aabb.max.z)
            backend.glEnd()
        }*/
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
        console.info("Particle ${particles.size}")
    }
}

private fun updateSnowflake(particle: Particle): Boolean {
    val snowflake = particle as Snowflake
    snowflake.position.y -= 0.01f
    snowflake.position.x = snowflake.origin.x + sin(snowflake.randomness + snowflake.position.y * 2f) * 0.5f
    snowflake.position.z = snowflake.origin.z + sin(snowflake.randomness + snowflake.position.y * 4f) * 0.2f
    return particle.position.y > -2f
}

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        controller.position.set(Vector3f(0f, 0f, 3f))
        console.info("Controller set..")
        billboardsTechnique.prepare(shadersLib)
        console.info("Particles ready..")
        textTechnique.prepare(shadersLib, texturesLib)
        console.info("Text ready..")
        snowflakeDiffuse = texturesLib.loadTexture("textures/snowflake.png")
        console.info("Texture loaded..")
        GlState.apply(color = Vector3f())
        console.success("All ready..")
    }

    override fun onDraw() {
        GlState.clear()
        particles.tick()
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
            billboardsTechnique.instance(particles, node, snowflakeDiffuse, BILLBOARDS_SIDE, BILLBOARDS_SIDE)
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