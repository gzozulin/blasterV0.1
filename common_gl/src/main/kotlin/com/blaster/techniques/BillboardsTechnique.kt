package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Mesh
import com.blaster.scene.Node
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

interface PositionsProvider {
    fun flush(buffer: ByteBuffer)
    fun count(): Int
}

// todo: parameter to billboard only around y axis - to draw characters and items
// todo: maybe (optionally) sort billboards?
// todo: buffers: positon, transparency, scale, rotation around z, should be standalone
// todo: mirror with uniform
class BillboardsTechnique(max: Int) {
    private lateinit var program: GlProgram
    private lateinit var rect: Mesh

    private val positionsBuffer = ByteBuffer.allocateDirect(max * 3 * 4) // max * vec3f
            .order(ByteOrder.nativeOrder())

    private lateinit var positionsGlBuffer: GlBuffer

    fun prepare(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram(
                "shaders/billboards/billboards.vert", "shaders/billboards/billboards.frag")
        positionsGlBuffer = GlBuffer(backend.GL_ARRAY_BUFFER, positionsBuffer, backend.GL_STREAM_DRAW)
        val additional = listOf(GlAttribute.ATTRIBUTE_BILLBOARD_POSITION to positionsGlBuffer)
        rect = Mesh.rect(additionalAttributes = additional)
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(program, rect, positionsGlBuffer)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            draw.invoke()
        }
    }

    fun instance(provider: PositionsProvider, node: Node, diffuse: GlTexture, width: Float, height: Float) {
        glBind(diffuse) {
            positionsGlBuffer.updateBuffer {
                provider.flush(it)
            }
            program.setUniform(GlUniform.UNIFORM_MODEL_M, node.calculateModelM())
            program.setUniform(GlUniform.UNIFORM_WIDTH, width)
            program.setUniform(GlUniform.UNIFORM_HEIGHT, height)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            rect.drawInstanced(instances = provider.count())
        }
    }
}