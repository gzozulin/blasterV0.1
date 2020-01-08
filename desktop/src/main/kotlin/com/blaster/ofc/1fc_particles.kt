package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.common.randomFloat
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import org.joml.Matrix4f
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.util.*

// todo: draw instanced billboards
// todo: glParticles (same as gl mesh)

private const val W = 800
private const val H = 600

private val backend = GlLocator.locate()

const val POINTS_CNT = 1000

private val random = Random()

class ParticlesTechnique {
    private lateinit var program: GlProgram
    private lateinit var pointsBuffer: GlBuffer

    private val attributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_IS_ALIVE)

    fun prepare(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/particles/particles.vert", "shaders/particles/particles.frag")
        val buffer = ByteBuffer.allocateDirect(POINTS_CNT * 4 * 4) // 1000 * vec4f
        val floats = buffer.asFloatBuffer()
        for (i in 0 until POINTS_CNT) {
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
            floats.put(randomFloat(-1f, 1f))
            floats.put(if (random.nextBoolean()) 1f else 0f)
        }
        buffer.position(0)
        pointsBuffer = GlBuffer(backend.GL_ARRAY_BUFFER, buffer, backend.GL_STATIC_DRAW)
    }

    fun draw(camera: Camera) {
        glBind(listOf(program, pointsBuffer)) {
            GlAttribute.bindVertexPointers(attributes)
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_MODEL_M, Matrix4f().identity()) // todo
            GlState.draw(backend.GL_POINTS, POINTS_CNT)
            GlAttribute.disableVertexPointers(attributes)
        }
    }
}

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)

private val technique = ParticlesTechnique()

private val camera = Camera(W.toFloat() / H.toFloat())

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        technique.prepare(shadersLib)
        camera.lookAt(Vector3f(-3f), Vector3f(0f))
        GlState.apply()
    }

    override fun onDraw() {
        GlState.clear()
        technique.draw(camera)
    }
}

fun main() {
    window.show()
}