package com.blaster.scene

import com.blaster.gl.*

private val backend = GlLocator.locate()

class Mesh(
        private val attributes: List<Pair<GlAttribute, GlBuffer>>,
        private val indicesBuffer: GlBuffer,
        private val indicesCount: Int) : GLBindable {

    private fun bindVertexPointers() {
        attributes.forEach {
            glCheck {
                backend.glEnableVertexAttribArray(it.first.location)
                it.second.bind()
                backend.glVertexAttribPointer(it.first.location, it.first.size, backend.GL_FLOAT, false, 0, 0)
                if (it.first.divisor != 0) {
                    backend.glVertexAttribDivisor(it.first.location, it.first.divisor)
                }
            }
        }
    }

    private fun disableVertexPointers() {
        attributes.forEach {
            it.second.unbind()
            glCheck { backend.glDisableVertexAttribArray(it.first.location) }
            if (it.first.divisor != 0) {
                backend.glVertexAttribDivisor(it.first.location, 0)
            }
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

    fun draw(mode: Int = backend.GL_TRIANGLES) {
        glCheck { backend.glDrawElements(mode, indicesCount, backend.GL_UNSIGNED_INT, 0) }
    }

    fun drawInstanced(mode: Int = backend.GL_TRIANGLES, instances: Int) {
        glCheck { backend.glDrawElementsInstanced(mode, indicesCount, backend.GL_UNSIGNED_INT, 0, instances) }
    }

    companion object {
        fun rect(additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): Mesh {
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
            val attributes = mutableListOf(
                    GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, quadPositions),
                    GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, quadTexCoords)
            )
            attributes.addAll(additionalAttributes)
            return Mesh(attributes, GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, quadIndices), quadIndices.size)
        }

        fun triangle(): Mesh {
            val trianglePositions = floatArrayOf(
                    0f,  1f, 0f,
                    -1f, -1f, 0f,
                    1f, -1f, 0f
            )
            val triangleTexCoords = floatArrayOf(
                    0.5f, 1f,
                    0f,   0f,
                    1f,   0f
            )
            val triangleIndices = intArrayOf(0, 1, 2)
            val attributes = listOf(
                    GlAttribute.ATTRIBUTE_POSITION to GlBuffer.create(backend.GL_ARRAY_BUFFER, trianglePositions),
                    GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer.create(backend.GL_ARRAY_BUFFER, triangleTexCoords)
            )
            return Mesh(attributes, GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, triangleIndices), triangleIndices.size)
        }
    }
}