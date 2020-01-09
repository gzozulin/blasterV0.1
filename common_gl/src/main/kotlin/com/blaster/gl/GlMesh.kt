package com.blaster.gl

private val backend = GlLocator.locate()

class GlMesh(
        private val attributes: List<Pair<GlAttribute, GlBuffer>>,
        private val indicesBuffer: GlBuffer,
        private val indicesCount: Int) : GLBindable {

    private fun bindVertexPointers() {
        attributes.forEach {
            glCheck {
                backend.glEnableVertexAttribArray(it.first.location)
                it.second.bind()
                backend.glVertexAttribPointer(it.first.location, it.first.size, backend.GL_FLOAT,
                        false, 0, 0)
            }
        }
    }

    private fun disableVertexPointers() {
        attributes.forEach {
            it.second.unbind()
            glCheck { backend.glDisableVertexAttribArray(it.first.location) }
        }
    }

    override fun bind() {
        bindVertexPointers()
        indicesBuffer.bind()
    }

    override fun unbind() {
        indicesBuffer.unbind()
        disableVertexPointers()
    }

    fun draw() {
        GlState.draw(count = indicesCount)
    }

    companion object {
        fun rect(): GlMesh {
            // todo: upside down, normalized device space?
            val quadPositions = floatArrayOf(
                    -1f,  1f, 0f,
                    -1f, -1f, 0f,
                     1f,  1f, 0f,
                     1f, -1f, 0f
            )
            val quadTexCoords = floatArrayOf(
                    0f, 1f,
                    0f, 0f,
                    1f, 1f,
                    1f, 0f
            )
            val quadIndices = intArrayOf(0, 1, 2, 1, 3, 2)
            return GlMesh(
                    listOf(
                            GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, quadPositions),
                            GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, quadTexCoords)
                    ),
                    GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, quadIndices), quadIndices.size
            )
        }

        fun triangle(): GlMesh {
            val trianglePositions = floatArrayOf(
                    0f,  1f, 0f,
                    -1f, -1f, 0f,
                    1f, -1f, 0f
            )
            val triangleTexCoords = floatArrayOf(
                    0.5f, 0f,
                    0f,   1f,
                    1f,   1f
            )
            val triangleIndices = intArrayOf(0, 1, 2)
            return GlMesh(
                    listOf(
                            GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, trianglePositions),
                            GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, triangleTexCoords)
                    ),
                    GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, triangleIndices), triangleIndices.size
            )
        }
    }
}