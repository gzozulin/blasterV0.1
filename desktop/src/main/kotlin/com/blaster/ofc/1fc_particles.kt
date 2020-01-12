package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.Console
import com.blaster.common.randomFloat
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

private const val W = 800
private const val H = 600

private val backend = GlLocator.locate()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

const val PARTICLES_MAX = 10000

private val random = Random()

// todo: ParticleSystem with BillboardTechnique
// todo: sort particles/bbrds?

class Particles(
        private val emitterFunction: (emitter: Vector3f, particles: MutableList<Vector3f>) -> Unit,
        private val particleFunction: (position: Vector3f) -> Boolean) {

    val emitters = mutableListOf<Vector3f>()
    val particles = mutableListOf<Vector3f>()

    fun tick() {
        emitters.forEach { emitterFunction.invoke(it, particles) }
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
        particles.forEach {
            floats.put(it.x)
            floats.put(it.y)
            floats.put(it.z)
        }
    }
}

class BillboardsTechnique {
    private lateinit var program: GlProgram
    private lateinit var rect: Mesh
    private lateinit var diffuse: GlTexture

    private val enabledBuffer = ByteBuffer.allocateDirect(PARTICLES_MAX * 1 * 4) // 1000 * float
            .order(ByteOrder.nativeOrder())
    private val positionsBuffer = ByteBuffer.allocateDirect(PARTICLES_MAX * 3 * 4) // 1000 * vec3f
            .order(ByteOrder.nativeOrder())

    private lateinit var enabledGlBuffer: GlBuffer

    fun prepare(shadersLib: ShadersLib, texturesLib: TexturesLib) {
        program = shadersLib.loadProgram(
                "shaders/billboards/billboards.vert", "shaders/billboards/billboards.frag")
        createPositions()
        updateEnabled(enabledBuffer)
        enabledGlBuffer = GlBuffer(backend.GL_ARRAY_BUFFER, enabledBuffer, backend.GL_STREAM_DRAW)
        val positionsGlBuffer = GlBuffer(backend.GL_ARRAY_BUFFER, positionsBuffer)
        val additional = listOf(GlAttribute.ATTRIBUTE_BILLBOARD_IS_ENABLED to enabledGlBuffer,
                GlAttribute.ATTRIBUTE_BILLBOARD_POSITION to positionsGlBuffer)
        rect = Mesh.rect(additionalAttributes = additional)
        diffuse = texturesLib.loadTexture("textures/winner.png")
    }

    private fun updateEnabled(buffer: ByteBuffer) {
        buffer.rewind()
        val floats = buffer.asFloatBuffer()
        for (i in 0 until PARTICLES_MAX) {
            floats.put(if (random.nextBoolean()) 1f else 0f)
        }
        buffer.position(0)
    }

    private fun createPositions() {
        val floats = positionsBuffer.asFloatBuffer()
        for (i in 0 until PARTICLES_MAX) {
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
        }
        positionsBuffer.position(0)
    }

    fun draw(camera: Camera) {
        glBind(enabledGlBuffer) {
            enabledGlBuffer.updateBuffer {
                updateEnabled(it)
            }
        }
        glBind(listOf(program, rect, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, Matrix4f().identity()) // todo
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = PARTICLES_MAX)
        }
    }
}

private val particlesTechnique = BillboardsTechnique()
private val textTechnique = TextTechnique()

private val console = Console(2000L)

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
        GlState.apply()
        console.success("All ready..")
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
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