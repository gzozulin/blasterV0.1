package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val SIZE_OF_FLOAT = 4
const val SIZE_OF_UINT = 4

class GLGeometry(
        vertices: FloatArray, indices: IntArray,
        private val verticesMap: List<GLAttribute>,
        private val mode: Int) {

    private val vboHandle: Int
    private val eboHandle: Int

    private val indicesSize: Int = indices.size.also { check(indices.isNotEmpty()) }

    init {
        val handles = IntArray(2)
        GLES20.glGenBuffers(2, handles, 0).also { checkForGLError() }
        vboHandle = handles[0]
        eboHandle = handles[1]
        check(vboHandle != 0)
        check(eboHandle != 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * SIZE_OF_FLOAT, allocateFloatBuffer(vertices), GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, eboHandle)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * SIZE_OF_UINT, allocateIntBuffer(indices), GLES20.GL_STATIC_DRAW)
    }

    private fun allocateFloatBuffer(vertices: FloatArray) =
            ByteBuffer.allocateDirect(vertices.size * SIZE_OF_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(vertices)
                    .position(0)

    private fun allocateIntBuffer(indices: IntArray) =
            ByteBuffer.allocateDirect(indices.size * SIZE_OF_UINT)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer()
                    .put(indices)
                    .position(0)

    fun bind() {
        var stride = 0
        var index = 0
        var offset = 0
        verticesMap.forEach { stride += it.size * SIZE_OF_FLOAT }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle).also { checkForGLError() }
        verticesMap.forEach {
            GLES20.glVertexAttribPointer(index, it.size, GLES20.GL_FLOAT, false, stride, offset).also { checkForGLError() }
            GLES20.glEnableVertexAttribArray(index).also { checkForGLError() }
            offset += it.size * SIZE_OF_FLOAT
            index++
        }
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, eboHandle).also { checkForGLError() }
    }

    fun draw() {
        GLES20.glDrawElements(mode, indicesSize, GLES20.GL_UNSIGNED_INT, 0).also { checkForGLError() }
    }
}