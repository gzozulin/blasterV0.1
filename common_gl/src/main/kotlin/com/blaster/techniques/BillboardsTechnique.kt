package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.auxiliary.mat4
import com.blaster.gl.*
import com.blaster.entity.Camera
import com.blaster.gl.GlMesh
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private val backend = GlLocator.locate()

// todo: the bad (integration)
interface BillboardsProvider {
    fun flushPositions(position: FloatBuffer)
    fun flushScale(scale: FloatBuffer)
    fun flushTransparency(transparency: FloatBuffer)
    fun count(): Int
}

// todo: parameter to billboard only around y axis - to draw characters and items
// todo: maybe (optionally) sort billboards?
// todo: buffers: rotation around z, color
// todo: mirror with uniform

class BillboardsTechnique(private val max: Int) {
    private lateinit var program: GlProgram
    private lateinit var rect: GlMesh

    private lateinit var positions: GlBuffer
    private lateinit var scale: GlBuffer
    private lateinit var transparency: GlBuffer

    fun prepare(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram(
                "shaders/billboards/billboards.vert", "shaders/billboards/billboards.frag")
        positions = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 3 * 4) // max * vec3f
                .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)
        scale = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 4) // max * float
                .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)
        transparency = GlBuffer(backend.GL_ARRAY_BUFFER, ByteBuffer.allocateDirect(max * 4) // max * float
                .order(ByteOrder.nativeOrder()), backend.GL_STREAM_DRAW)
        val additional = listOf(
                GlAttribute.ATTRIBUTE_BILLBOARD_POSITION to positions,
                GlAttribute.ATTRIBUTE_BILLBOARD_SCALE to scale,
                GlAttribute.ATTRIBUTE_BILLBOARD_TRANSPARENCY to transparency)
        rect = GlMesh.rect(additionalAttributes = additional)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(program, rect)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            draw.invoke()
        }
    }

    // Mapping the buffer with positions, updating the buffer from provider
    private fun updatePositions(provider: BillboardsProvider) {
        glBind(positions) {
            positions.updateBuffer {
                provider.flushPositions(it.asFloatBuffer())
            }
        }
    }

    private fun updateScales(provider: BillboardsProvider) {
        glBind(scale) {
            scale.updateBuffer {
                provider.flushScale(it.asFloatBuffer())
            }
        }
    }

    private fun updateTransparency(provider: BillboardsProvider) {
        glBind(transparency) {
            transparency.updateBuffer {
                provider.flushTransparency(it.asFloatBuffer())
            }
        }
    }

    fun instance(provider: BillboardsProvider, modelM: mat4, diffuse: GlTexture, width: Float, height: Float,
                 updateScale: Boolean = true, updateTransparency: Boolean = true) {
        updatePositions(provider)
        // Scale and transparency updated conditionally
        if (updateScale) {
            updateScales(provider)
        }
        if (updateTransparency) {
            updateTransparency(provider)
        }
        // After data is uploaded, performing an instanced draw pass
        glBind(listOf(diffuse, positions, scale, transparency)) {
            program.setUniform(GlUniform.UNIFORM_SCALE_FLAG, if (updateScale) 1 else 0)
            program.setUniform(GlUniform.UNIFORM_TRANSPARENCY_FLAG, if (updateTransparency) 1 else 0)
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setUniform(GlUniform.UNIFORM_WIDTH, width)
            program.setUniform(GlUniform.UNIFORM_HEIGHT, height)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = provider.count())
        }
    }
}