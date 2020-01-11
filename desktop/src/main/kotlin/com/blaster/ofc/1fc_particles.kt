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

const val POINTS_CNT = 3

private val random = Random()

// todo: ParticleSystem with BillboardTechnique

class BillboardsTechnique {
    private lateinit var program: GlProgram
    private lateinit var rect: GlMesh
    private lateinit var diffuse: GlTexture

    fun prepare(shadersLib: ShadersLib, texturesLib: TexturesLib) {
        program = shadersLib.loadProgram(
                "shaders/particles/particles.vert", "shaders/particles/particles.frag")
        rect = GlMesh.rect(listOf(createParticleIsAlive(), createParticlePositions()))
        diffuse = texturesLib.loadTexture("textures/winner.png")
    }

    private fun createParticleIsAlive(): Pair<GlAttribute, GlBuffer> {
        val buffer = ByteBuffer.allocateDirect(POINTS_CNT * 1 * 4) // 1000 * float
                .order(ByteOrder.nativeOrder())
        val floats = buffer.asFloatBuffer()
        for (i in 0 until POINTS_CNT) {
            //floats.put(if (random.nextBoolean()) 1f else 0f)
            floats.put(1f)
        }
        buffer.position(0)
        return GlAttribute.ATTRIBUTE_PARTICLE_IS_ALIVE to GlBuffer(backend.GL_ARRAY_BUFFER, buffer)
    }

    private fun createParticlePositions(): Pair<GlAttribute, GlBuffer> {
        val buffer = ByteBuffer.allocateDirect(POINTS_CNT * 3 * 4) // 1000 * vec3f
                .order(ByteOrder.nativeOrder())
        val floats = buffer.asFloatBuffer()
        for (i in 0 until POINTS_CNT) {
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
        }
        buffer.position(0)
        return GlAttribute.ATTRIBUTE_PARTICLE_POSITION to  GlBuffer(backend.GL_ARRAY_BUFFER, buffer)
    }

    fun draw(camera: Camera) {
        glBind(listOf(program, rect, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, Matrix4f().identity()) // todo
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_VIEW_POS, camera.position)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = POINTS_CNT)
        }
    }
}

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

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
        }
    }

    override fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.w = false
            GLFW.GLFW_KEY_A -> controller.a = false
            GLFW.GLFW_KEY_S -> controller.s = false
            GLFW.GLFW_KEY_D -> controller.d = false
        }
    }
}

fun main() {
    window.show()
}