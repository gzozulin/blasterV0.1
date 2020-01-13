package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.AABB
import com.blaster.common.Console
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.scene.Mesh
import com.blaster.techniques.TextTechnique
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.sin

private const val W = 800
private const val H = 600

private val backend = GlLocator.locate()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

private val sceneAABB = AABB(Vector3f(-5f), Vector3f(5f))

const val BILLBOARDS_MAX = 1000
const val BILLBOARDS_WIDTH = 0.1f
const val BILLBOARDS_HEIGHT = 0.1f

private val random = Random()

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

open class Particle(origin: Vector3f) {
    val position = Vector3f(origin)
}

class Particles(
        private val max: Int,
        private val emitters: List<Vector3f>,
        private val emitterFunction: (emitter: Vector3f, particles: MutableList<Particle>) -> Unit,
        private val particleFunction: (particle: Particle) -> Boolean) {


    val particles = mutableListOf<Particle>()

    fun tick() {
        emitters.forEach {
            if (particles.size < max) {
                emitterFunction.invoke(it, particles)
            }
        }
        val particlesIterator = particles.iterator()
        while (particlesIterator.hasNext()) {
            val isAlive = particleFunction.invoke(particlesIterator.next())
            if (!isAlive) {
                particlesIterator.remove()
            }
        }
    }

    fun flush(buffer: ByteBuffer) {
        buffer.rewind()
        val floats = buffer.asFloatBuffer()
        particles.forEachIndexed { index, particle ->
            if (index >= max) {
                return
            }
            floats.put(particle.position.x)
            floats.put(particle.position.y)
            floats.put(particle.position.z)
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

private val particles = Particles(
        BILLBOARDS_MAX,
        snowflakeEmitters(),
        ::emitSnowflake,
        ::updateSnowflake)

// todo: parameter to billboard only around y axis - to draw characters and items
// todo: maybe (optionally) sort billboards?
// todo: can draw multiple sets of billboards with same buffer
class BillboardsTechnique(max: Int) {
    private lateinit var program: GlProgram
    private lateinit var rect: Mesh
    private lateinit var diffuse: GlTexture

    private val positionsBuffer = ByteBuffer.allocateDirect(max * 3 * 4) // max * vec3f
            .order(ByteOrder.nativeOrder())

    private lateinit var positionsGlBuffer: GlBuffer

    fun prepare(shadersLib: ShadersLib, texturesLib: TexturesLib) {
        program = shadersLib.loadProgram(
                "shaders/billboards/billboards.vert", "shaders/billboards/billboards.frag")
        positionsGlBuffer = GlBuffer(backend.GL_ARRAY_BUFFER, positionsBuffer, backend.GL_STREAM_DRAW)
        val additional = listOf(GlAttribute.ATTRIBUTE_BILLBOARD_POSITION to positionsGlBuffer)
        rect = Mesh.rect(additionalAttributes = additional)
        diffuse = texturesLib.loadTexture("textures/snowflake.png")
    }

    fun draw(camera: Camera) {
        particles.tick()
        glBind(listOf(positionsGlBuffer)) {
            positionsGlBuffer.updateBuffer {
                particles.flush(it)
            }
        }
        glBind(listOf(program, rect, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, Matrix4f().identity()) // todo
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            program.setUniform(GlUniform.UNIFORM_WIDTH, BILLBOARDS_WIDTH)
            program.setUniform(GlUniform.UNIFORM_HEIGHT, BILLBOARDS_HEIGHT)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = particles.particles.size)
        }
    }
}

private val immediateTechnique = ImmediateTechnique()
private val particlesTechnique = BillboardsTechnique(BILLBOARDS_MAX)
private val textTechnique = TextTechnique()

private val console = Console(1000L)

private val camera = Camera(W.toFloat() / H.toFloat())

private val controller = Controller()

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        controller.position.set(Vector3f(0f, 0f, 3f))
        console.info("Controller set..")
        particlesTechnique.prepare(shadersLib, texturesLib)
        console.info("Particles ready..")
        textTechnique.prepare(shadersLib, texturesLib)
        console.info("Text ready..")
        GlState.apply(color = Vector3f())
        console.success("All ready..")
    }

    override fun onDraw() {
        GlState.clear()
        immediateTechnique.aabb(camera, sceneAABB)
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        console.throttle()
        textTechnique.draw {
            console.render { position, text, color, scale ->
                textTechnique.text(text, position, scale, color)
            }
        }
        particlesTechnique.draw(camera)
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