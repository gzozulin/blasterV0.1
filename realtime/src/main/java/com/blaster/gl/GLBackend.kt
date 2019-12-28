package com.blaster.gl

class GLBackendLocator {
    companion object {
        private val inst: GLBackend

        init {
            val clazz = Class.forName("com.blaster.gl.GLBackendImpl")
            val ctor = clazz.getConstructor()
            inst = ctor.newInstance() as GLBackend
        }

        fun instance() = inst
    }

}

interface GLBackend {
    val GL_NO_ERROR: Int
    fun glGetError(): Int
    fun gluErrorString(error: Int): String

    val GL_ARRAY_BUFFER: Int
    val GL_ELEMENT_ARRAY_BUFFER: Int
    val GL_STATIC_DRAW: Int
    fun glGenBuffers(n: Int, buffers: IntArray, offset: Int)
    fun glBindBuffer(target: Int, buffer: Int)
    fun glBufferData(target: Int, size: Int, data: java.nio.Buffer, usage: Int)
}