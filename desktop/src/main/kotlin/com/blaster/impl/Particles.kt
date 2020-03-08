package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.toolbox.Console
import com.blaster.auxiliary.center
import com.blaster.auxiliary.lerpf
import com.blaster.auxiliary.mat4
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.entity.Camera
import com.blaster.entity.Controller
import com.blaster.toolbox.Particle
import com.blaster.toolbox.Particles
import com.blaster.techniques.BillboardsTechnique
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.TextTechnique
import org.joml.AABBf
import org.joml.Vector2f
import org.joml.Vector3f
import java.util.*
import kotlin.math.sin

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

private val camera = Camera()
private val controller = Controller(Vector3f(0f, 0f, 3f), velocity = 0.05f)
private val wasd = WasdInput(controller)

private val identityM = mat4()

private var mouseControl = false

class Snowflake(origin: Vector3f) : Particle(origin) {
    val origin = Vector3f(origin)
    val randomness = random.nextFloat()

    init {
        transparency = 0.5f + random.nextFloat() / 2f
    }
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
    val ttl = 1f - particle.position.y
    particle.transparency = lerpf(1f, 0f, ttl)
    particle.scale -= 0.001f
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
    val ttl = (2f - particle.position.y) / 1.5f
    if (ttl > 0.5f) {
        particle.transparency = lerpf(1f, 0f, ttl)
    } else {
        particle.transparency = lerpf(0f, 1f, ttl)
    }
    particle.scale += 0.003f
    return particle.position.y < 2f
}

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        billboardsTechnique.prepare(shadersLib)
        console.info("Particles ready..")
        textTechnique.create(shadersLib, texturesLib)
        console.info("Techniques ready..")
        snowflakeDiffuse = texturesLib.loadTexture("textures/snowflake.png")
        console.info("Texture loaded: textures/snowflake.png")
        flameDiffuse = texturesLib.loadTexture("textures/flame.png")
        flameDiffuse2 = texturesLib.loadTexture("textures/flame.png", mirror = true)
        console.info("Texture loaded: textures/flame.png")
        smokeDiffuse = texturesLib.loadTexture("textures/smoke.png")
        smokeDiffuse2 = texturesLib.loadTexture("textures/smoke.png", mirror = true)
        console.info("Texture loaded: textures/smoke.png")
        console.success("All ready..")
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height, color = Vector3f())
        camera.setPerspective(width, height)
        immediateTechnique.resize(camera)
    }

    override fun onTick() {
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
        immediateTechnique.aabb(camera, sceneAABB, mat4())
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
            }
        }
        billboardsTechnique.draw(camera) {
            GlState.drawTransparent {
                billboardsTechnique.instance(snow, identityM, snowflakeDiffuse, SNOWFLAKE_SIDE, SNOWFLAKE_SIDE, updateScale = false)
                GlState.drawWithNoDepth {
                    billboardsTechnique.instance(flame, identityM, flameDiffuse, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(smoke, identityM, smokeDiffuse, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(flame2, identityM, flameDiffuse2, FLAMES_SIDE, FLAMES_SIDE)
                    billboardsTechnique.instance(smoke2, identityM, smokeDiffuse2, FLAMES_SIDE, FLAMES_SIDE)
                }
            }
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        if (mouseControl) {
            wasd.onCursorDelta(delta)
        }
    }

    override fun mouseBtnPressed(btn: Int) {
        mouseControl = true
    }

    override fun mouseBtnReleased(btn: Int) {
        mouseControl = false
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
