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

    override fun bind() {
        indicesBuffer.bind()
        verticesBuffer.bind()
        GlAttribute.bindVertexPointers(attributes)
    }

    override fun unbind() {
        GlAttribute.disableVertexPointers(attributes)
        verticesBuffer.unbind()
        indicesBuffer.unbind()
    }

    fun draw() {
        GlState.draw(count = indicesCount)
    }

    companion object {
        fun rect(): GlMesh {
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

        fun triangle(): GlMesh {
            val triangleAttributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD)
            val triangleVertices = floatArrayOf(
                    0f,  1f, 0f,     0.5f, 0f,
                    -1f, -1f, 0f,     0f,   1f,
                    1f, -1f, 0f,     1f,   1f
            )
            val triangleIndices = intArrayOf(0, 1, 2)
            return GlMesh(triangleVertices, triangleIndices, triangleAttributes)
        }
    }
}