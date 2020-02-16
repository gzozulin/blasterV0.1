package com.blaster.gl

private val backend = GlLocator.locate()

class GlMesh(
        private val attributes: List<Pair<GlAttribute, GlBuffer>>,
        private val indicesBuffer: GlBuffer,
        private val indicesCount: Int) : GlBindable {

    private var handle: Int? = null

    init {
        createVAO()
    }

    private fun createVAO() {
        handle = glCheck { backend.glGenVertexArrays() }
        check(handle!! > 0)
        glBind(this) { bindVertexPointers() }
    }

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
        indicesBuffer.bind()
    }

    override fun bind() {
        try {
            glCheck { backend.glBindVertexArray(handle!!) }
        } catch (e: GlError) {
            createVAO() // by some reason happens after resize
            bind()
        }
    }

    override fun unbind() {
        glCheck { backend.glBindVertexArray(0) }
    }

    fun draw(mode: Int = backend.GL_TRIANGLES) {
        glCheck { backend.glDrawElements(mode, indicesCount, backend.GL_UNSIGNED_INT, 0) }
    }

    fun drawInstanced(mode: Int = backend.GL_TRIANGLES, instances: Int) {
        glCheck { backend.glDrawElementsInstanced(mode, indicesCount, backend.GL_UNSIGNED_INT, 0, instances) }
    }

    companion object {
        fun rect(additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): GlMesh {
            val vertices = floatArrayOf(
                    -1f,  1f, 0f,
                    -1f, -1f, 0f,
                     1f,  1f, 0f,
                     1f, -1f, 0f)
            val texCoords = floatArrayOf(
                    0f, 1f,
                    0f, 0f,
                    1f, 1f,
                    1f, 0f)
            val indices = intArrayOf(0, 1, 2, 1, 3, 2)
            val attributes = mutableListOf(
                    GlAttribute.ATTRIBUTE_POSITION to
                            GlBuffer.create(backend.GL_ARRAY_BUFFER, vertices),
                    GlAttribute.ATTRIBUTE_TEXCOORD to
                            GlBuffer.create(backend.GL_ARRAY_BUFFER, texCoords))
            attributes.addAll(additionalAttributes)
            return GlMesh(attributes,
                    GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, indices), indices.size)
        }

        fun triangle(additionalAttributes: List<Pair<GlAttribute, GlBuffer>> = listOf()): GlMesh {
            val vertices = floatArrayOf(
                     0f,  1f, 0f,
                    -1f, -1f, 0f,
                     1f, -1f, 0f)
            val texCoords = floatArrayOf(
                    0.5f, 1f,
                    0f,   0f,
                    1f,   0f)
            val indices = intArrayOf(0, 1, 2)
            val attributes = mutableListOf(
                    GlAttribute.ATTRIBUTE_POSITION to
                            GlBuffer.create(backend.GL_ARRAY_BUFFER, vertices),
                    GlAttribute.ATTRIBUTE_TEXCOORD to
                            GlBuffer.create(backend.GL_ARRAY_BUFFER, texCoords))
            attributes.addAll(additionalAttributes)
            return GlMesh(attributes,
                    GlBuffer.create(backend.GL_ELEMENT_ARRAY_BUFFER, indices), indices.size)
        }
    }
}