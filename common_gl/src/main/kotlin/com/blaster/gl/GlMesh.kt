package com.blaster.gl

private val backend = GlLocator.locate()

class GlMesh(
        private val verticesBuffer: GlBuffer,
        private val indicesBuffer: GlBuffer,
        private val indicesCount: Int,
        private val attributes: List<GlAttribute>) : GLBindable {

    constructor(vertices: FloatArray, indices: IntArray, attributes: List<GlAttribute>)
            : this(
                GlBuffer.create(backend.GL_ARRAY_BUFFER, vertices),
                GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, indices), indices.size, attributes)

    private fun bindVertexPointers() {
        var stride = 0
        var offset = 0L
        attributes.forEach { stride += it.size * 4 }
        attributes.forEach {
            glCheck {
                backend.glEnableVertexAttribArray(it.location)
                backend.glVertexAttribPointer(it.location, it.size, backend.GL_FLOAT, false, stride, offset)
            }
            offset += it.size * 4
        }
    }

    private fun disableVertexPointers() {
        attributes.forEach {
            glCheck { backend.glDisableVertexAttribArray(it.location) }
        }
    }

    override fun bind() {
        indicesBuffer.bind()
        verticesBuffer.bind()
        bindVertexPointers()
    }

    override fun unbind() {
        disableVertexPointers()
        verticesBuffer.unbind()
        indicesBuffer.unbind()
    }

    fun draw(mode: Int = backend.GL_TRIANGLES) {
        glCheck { backend.glDrawElements(mode, indicesCount, backend.GL_UNSIGNED_INT, 0) }
    }

    companion object {
        fun rectPosTex(): GlMesh {
            // todo: upside down, normalized device space?
            val quadAttributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD)
            val quadVertices = floatArrayOf(
                    -1f,  1f, 0f,     0f, 1f,
                    -1f, -1f, 0f,     0f, 0f,
                     1f,  1f, 0f,     1f, 1f,
                     1f, -1f, 0f,     1f, 0f
            )
            val quadIndices = intArrayOf(0, 1, 2, 1, 3, 2)
            return GlMesh(quadVertices, quadIndices, quadAttributes)
        }
    }
}