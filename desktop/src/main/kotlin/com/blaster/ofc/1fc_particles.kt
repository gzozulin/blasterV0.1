package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.randomFloat
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.scene.Camera
import org.joml.Matrix4f
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
    private lateinit var rect: GlMesh
    private lateinit var diffuse: GlTexture

    fun prepare(shadersLib: ShadersLib, texturesLib: TexturesLib) {
        program = shadersLib.loadProgram(
                "shaders/particles/particles.vert", "shaders/particles/particles.frag")
        rect = GlMesh.rect(listOf(createParticleIsAlive(), createParticlePositions()))
        diffuse = texturesLib.loadTexture("textures/winner.png")
    }

    private fun createParticleIsAlive(): Pair<GlAttribute, GlBuffer> {
        val bufferParticlePositions = ByteBuffer.allocateDirect(POINTS_CNT * 1 * 4) // 1000 * float
                .order(ByteOrder.nativeOrder())
        val bufferParticlePositionsFloats = bufferParticlePositions.asFloatBuffer()
        for (i in 0 until POINTS_CNT) {
            bufferParticlePositionsFloats.put(if (random.nextBoolean()) 1f else 0f)
        }
        bufferParticlePositions.position(0)
        return GlAttribute.ATTRIBUTE_PARTICLE_IS_ALIVE to GlBuffer(backend.GL_ARRAY_BUFFER, bufferParticlePositions)
    }

    private fun createParticlePositions(): Pair<GlAttribute, GlBuffer> {
        val bufferParticlePositions = ByteBuffer.allocateDirect(POINTS_CNT * 3 * 4) // 1000 * vec3f
                .order(ByteOrder.nativeOrder())
        val bufferParticlePositionsFloats = bufferParticlePositions.asFloatBuffer()
        for (i in 0 until POINTS_CNT) {
            bufferParticlePositionsFloats.put(randomFloat(-1f, 1f))
            bufferParticlePositionsFloats.put(randomFloat(-1f, 1f))
            bufferParticlePositionsFloats.put(randomFloat(-1f, 1f))
        }
        bufferParticlePositions.position(0)
        return GlAttribute.ATTRIBUTE_PARTICLE_POSITION to  GlBuffer(backend.GL_ARRAY_BUFFER, bufferParticlePositions)
    }

    fun draw(camera: Camera) {
        glBind(listOf(program, rect, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, Matrix4f().identity()) // todo
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = POINTS_CNT)
        }
    }
}

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)

private val technique = ParticlesTechnique()

private val camera = Camera(W.toFloat() / H.toFloat())

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        technique.prepare(shadersLib, texturesLib)
        camera.lookAt(Vector3f(0f, 0f, 2.5f), Vector3f(0f))
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