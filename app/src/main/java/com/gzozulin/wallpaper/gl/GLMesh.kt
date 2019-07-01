package com.gzozulin.wallpaper.gl

import android.opengl.GLES30

class GLMesh(
        vertices: FloatArray, indices: IntArray,
        attributes: List<GLAttribute>,
        private val mode: Int = GLES30.GL_TRIANGLES) : GLBindable {

    private val verticesBuffer: GLBuffer = GLBuffer(GLES30.GL_ARRAY_BUFFER, vertices)
    private val indicesBuffer: GLBuffer = GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices)

    private val indicesCount = indices.size

    init {
        glBind(verticesBuffer) {
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
    }

    override fun bind() {
        verticesBuffer.bind()
        indicesBuffer.bind()
    }

    override fun unbind() {
        verticesBuffer.unbind()
        indicesBuffer.unbind()
    }

    fun draw() {
        glCheck { GLES30.glDrawElements(mode, indicesCount, GLES30.GL_UNSIGNED_INT, 0) }
    }
}