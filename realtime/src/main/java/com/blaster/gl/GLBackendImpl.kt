package com.blaster.gl

import android.opengl.GLES30
import android.opengl.GLU
import java.nio.Buffer

class GLBackendImpl : GLBackend {
    override val GL_NO_ERROR: Int
        get() = GLES30.GL_NO_ERROR

    override fun gluErrorString(error: Int) = GLU.gluErrorString(error)
    override fun glGetError() = GLES30.glGetError()

    override val GL_ARRAY_BUFFER: Int
        get() = GLES30.GL_ARRAY_BUFFER
    override val GL_ELEMENT_ARRAY_BUFFER: Int
        get() = GLES30.GL_ELEMENT_ARRAY_BUFFER
    override val GL_STATIC_DRAW: Int
        get() = GLES30.GL_STATIC_DRAW

    override fun glGenBuffers(n: Int, buffers: IntArray, offset: Int) = GLES30.glGenBuffers(n, buffers, offset)
    override fun glBindBuffer(target: Int, buffer: Int) = GLES30.glBindBuffer(target, buffer)
    override fun glBufferData(target: Int, size: Int, data: Buffer, usage: Int) = GLES30.glBufferData(target, size, data, usage)
}