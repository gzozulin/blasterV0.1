package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Mesh
import com.blaster.scene.Node
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private val backend = GlLocator.locate()

interface BillboardsProvider {
    fun flushPositions(position: FloatBuffer)
    fun flushScale(scale: FloatBuffer)
    fun flushTransparency(transparency: FloatBuffer)
    fun count(): Int
}

// todo: parameter to billboard only around y axis - to draw characters and items
// todo: maybe (optionally) sort billboards?
// todo: buffers: positon, transparency, scale, rotation around z, color: should be standalone
// todo: mirror with uniform

class BillboardsTechnique(private val max: Int) {
    private lateinit var program: GlProgram
    private lateinit var rect: Mesh

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
        rect = Mesh.rect(additionalAttributes = additional)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(program, rect)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            draw.invoke()
        }
    }

    fun instance(provider: BillboardsProvider, node: Node, diffuse: GlTexture, width: Float, height: Float) {
        // todo: do it in one go somehow
        glBind(positions) {
            positions.updateBuffer {
                provider.flushPositions(it.asFloatBuffer())
            }
        }
        glBind(scale) {
            scale.updateBuffer {
                provider.flushScale(it.asFloatBuffer())
            }
        }
        glBind(transparency) {
            transparency.updateBuffer {
                provider.flushTransparency(it.asFloatBuffer())
            }
        }
        glBind(diffuse) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, node.calculateModelM())
            program.setUniform(GlUniform.UNIFORM_WIDTH, width)
            program.setUniform(GlUniform.UNIFORM_HEIGHT, height)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = provider.count())
        }
    }
}