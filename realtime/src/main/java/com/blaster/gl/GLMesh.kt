package com.blaster.gl

import android.opengl.GLES30

class GLMesh(
        private val verticesBuffer: GLBuffer,
        private val indicesBuffer: GLBuffer,
        private val indicesCount: Int,
        private val attributes: List<GLAttribute>) : GLBindable {

    constructor(vertices: FloatArray, indices: IntArray, attributes: List<GLAttribute>)
            : this(GLBuffer(GLES30.GL_ARRAY_BUFFER, vertices), GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices), indices.size, attributes)

    private fun bindVertexPointers() {
        var stride = 0
        var offset = 0
        attributes.forEach { stride += it.size * 4 }
        attributes.forEach {
            glCheck {
                GLES30.glEnableVertexAttribArray(it.location)
                GLES30.glVertexAttribPointer(it.location, it.size, GLES30.GL_FLOAT, false, stride, offset)
            }
            offset += it.size * 4
        }
    }

    private fun disableVertexPointers() {
        attributes.forEach {
            glCheck { GLES30.glDisableVertexAttribArray(it.location) }
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

    fun draw(mode: Int = GLES30.GL_TRIANGLES) {
        glCheck { GLES30.glDrawElements(mode, indicesCount, GLES30.GL_UNSIGNED_INT, 0) }
    }
}