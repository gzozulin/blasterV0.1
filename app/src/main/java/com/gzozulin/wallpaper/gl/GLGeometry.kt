package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLGeometry(vertices: FloatArray, indices: IntArray, private val mode: Int) {

    private val vboHandle: Int
    private val eboHandle: Int

    private val indicesSize: Int = indices.size.also { check(indices.isNotEmpty()) }

    init {
        val handles = IntArray(2)
        GLES20.glGenBuffers(2, handles, 0).also { checkForGLError() }
        vboHandle = handles[0]
        eboHandle = handles[1]
        check(vboHandle != 0 && eboHandle != 0)
    }

    init {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle).also { checkForGLError() }
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, allocateFloatBuffer(vertices), GLES20.GL_STATIC_DRAW)
                .also { checkForGLError() }
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, eboHandle).also { checkForGLError() }
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, allocateIntBuffer(indices), GLES20.GL_STATIC_DRAW)
                .also { checkForGLError() }
    }

    private fun allocateFloatBuffer(vertices: FloatArray) =
            ByteBuffer.allocateDirect(vertices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertices)
                    .position(0)

    private fun allocateIntBuffer(indices: IntArray) =
            ByteBuffer.allocateDirect(indices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer()
                    .put(indices)
                    .position(0)

    fun bind() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle).also { checkForGLError() }
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, eboHandle).also { checkForGLError() }
    }

    fun draw() {
        GLES20.glDrawElements(mode, indicesSize, GLES20.GL_UNSIGNED_INT, 0).also { checkForGLError() }
    }
}