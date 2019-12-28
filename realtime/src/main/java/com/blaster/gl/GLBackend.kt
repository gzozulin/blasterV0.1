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

    val GL_FRAMEBUFFER: Int
    val GL_RENDERBUFFER: Int
    val GL_FRAMEBUFFER_COMPLETE: Int
    fun glGenFramebuffers(n: Int, framebuffers: IntArray, offset: Int)
    fun glBindFramebuffer(target: Int, framebuffer: Int)
    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)
    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int)
    fun glDrawBuffers(n: Int, bufs: IntArray, offset: Int)
    fun glCheckFramebufferStatus(target: Int): Int
}