package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.Console
import com.blaster.common.center
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.scene.*
import com.blaster.techniques.BillboardsTechnique
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.TextTechnique
import org.joml.AABBf
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
private lateinit var flameDiffuse: GlTexture
private lateinit var flameDiffuse2: GlTexture
private lateinit var smokeDiffuse: GlTexture
private lateinit var smokeDiffuse2: GlTexture

private val sceneAABB = AABBf(Vector3f(-5f), Vector3f(5f))

const val BILLBOARDS_MAX = 2500

const val SNOWFLAKE_SIDE = 0.1f
const val FLAMES_SIDE = 1.5f

private val random = Random()

private val immediateTechnique = ImmediateTechnique()
private val billboardsTechnique = BillboardsTechnique(BILLBOARDS_MAX)
private val textTechnique = TextTechnique()

private val snow = Particles(BILLBOARDS_MAX, snowflakeEmitters(), ::emitSnowflake, ::updateSnowflake)
private val flame = Particles(BILLBOARDS_MAX, listOf(sceneAABB.center()), ::emitFlame, ::updateFlame)
private val flame2 = Particles(BILLBOARDS_MAX, listOf(sceneAABB.center()), ::emitFlame, ::updateFlame)
private val smoke = Particles(BILLBOARDS_MAX, listOf(sceneAABB.center().add(Vector3f(0f, 0.5f, 0f))),
        ::emitSmoke, ::updateSmoke)
private val smoke2 = Particles(BILLBOARDS_MAX, listOf(sceneAABB.center().add(Vector3f(0f, 0.5f, 0f))),
        ::emitSmoke, ::updateSmoke)

private val console = Console(1000L)

private val camera = Camera(W.toFloat() / H.toFloat())
private val controller = Controller(velocity = 0.05f)
private val node = Node()

class Snowflake(origin: Vector3f) : Particle(origin) {
    val origin = Vector3f(origin)
    val randomness = random.nextFloat()
}

private fun snowflakeEmitters(): List<Vector3f> {
    val emitters = mutableListOf<Vector3f>()
    for (x in -5..5) {
        for (z in -5..5) {
            emitters.add(Vector3f(x.toFloat(), sceneAABB.maxY, z.toFloat()))
        }
    }
    return emitters
}

private fun emitSnowflake(emitter: Vector3f, particles: MutableList<Particle>) {
    if (random.nextInt(50) == 1) {
        particles.add(Snowflake(emitter))
    }
}

private fun updateSnowflake(particle: Particle): Boolean {
    val snowflake = particle as Snowflake
    snowflake.position.y -= 0.01f
    snowflake.position.x = snowflake.origin.x + sin((snowflake.randomness + snowflake.position.y) * 2f) * 0.5f
    snowflake.position.z = snowflake.origin.z + sin((snowflake.randomness + snowflake.position.y) * 4f) * 0.2f
    return particle.position.y > sceneAABB.minY
}

private fun emitFlame(emitter: Vector3f, particles: MutableList<Particle>) {
    if (random.nextInt(5) == 1) {
        particles.add(Particle(emitter))
    }
}

private fun updateFlame(particle: Particle): Boolean {
    particle.position.y += 0.05f
    particle.position.x += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    particle.position.z += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    return particle.position.y < 1f
}

private fun emitSmoke(emitter: Vector3f, particles: MutableList<Particle>) {
    if (random.nextInt(70) == 1) {
        particles.add(Particle(emitter))
    }
}

private fun updateSmoke(particle: Particle): Boolean {
    particle.position.y += 0.01f
    particle.position.x += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    particle.position.z += (random.nextFloat() * if (random.nextBoolean()) 1f else -1f) * 0.01f
    return particle.position.y < 1.5f
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
        flameDiffuse2 = texturesLib.loadTexture("textures/flame.png", mirror = true)
        smokeDiffuse = texturesLib.loadTexture("textures/smoke.png")
        smokeDiffuse2 = texturesLib.loadTexture("textures/smoke.png", mirror = true)
        console.info("Textures loaded..")
        GlState.apply(color = Vector3f())
        console.success("All ready..")
    }

    override fun onDraw() {
        GlState.clear()
        snow.tick()
        flame.tick()
        flame2.tick()
        smoke.tick()
        smoke2.tick()
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
            billboardsTechnique.instance(snow, node, snowflakeDiffuse, SNOWFLAKE_SIDE, SNOWFLAKE_SIDE)
            GlState.drawTransparent {
                GlState.drawWithNoDepth {
                    billboardsTechnique.instance(flame, node, flameDiffuse, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(smoke, node, smokeDiffuse, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(flame2, node, flameDiffuse2, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(smoke2, node, smokeDiffuse2, FLAMES_SIDE, FLAMES_SIDE)
                }
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
