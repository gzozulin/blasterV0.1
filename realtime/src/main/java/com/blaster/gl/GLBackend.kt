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
    val GL_TRUE: Int
    val GL_FALSE: Int
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

    val GL_FLOAT: Int
    val GL_UNSIGNED_INT: Int
    val GL_TRIANGLES: Int
    fun glEnableVertexAttribArray(index: Int)
    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int)
    fun glDisableVertexAttribArray(index: Int)
    fun glDrawElements(mode: Int, count: Int, type: Int, offset: Int)

    val GL_VERTEX_SHADER: Int
    val GL_FRAGMENT_SHADER: Int
    val GL_COMPILE_STATUS: Int
    val GL_LINK_STATUS: Int
    fun glCreateShader(type: Int): Int
    fun glShaderSource(shader: Int, string: String)
    fun glCompileShader(shader: Int)
    fun glGetShaderiv(shader: Int, pname: Int, params: IntArray, offset: Int)
    fun glGetShaderInfoLog(shader: Int): String
    fun glDeleteShader(shader: Int)
    fun glCreateProgram(): Int
    fun glAttachShader(program: Int, shader: Int)
    fun glLinkProgram(program: Int)
    fun glGetProgramiv(program: Int, pname: Int, params: IntArray, offset: Int)
    fun glGetProgramInfoLog(program: Int): String
    fun glGetUniformLocation(program: Int, name: String): Int
    fun glDeleteProgram(program: Int)
    fun glUseProgram(program: Int)
    fun glUniform1i(location: Int, x: Int)
    fun glUniform1f(location: Int, x: Float)
    fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int)
    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: java.nio.FloatBuffer)
}