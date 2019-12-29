package com.blaster.gl

class GLLocator {
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

    val GL_TRUE: Int
    val GL_FALSE: Int
    val GL_DEPTH_TEST: Int
    val GL_COLOR_BUFFER_BIT: Int
    val GL_DEPTH_BUFFER_BIT: Int
    val GL_CCW: Int
    val GL_CULL_FACE: Int
    fun glEnable(cap: Int)
    fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun glViewport(x: Int, y: Int, width: Int, height: Int)
    fun glFrontFace(mode: Int)
    fun glClear(mask: Int)

    val GL_ARRAY_BUFFER: Int
    val GL_ELEMENT_ARRAY_BUFFER: Int
    val GL_STATIC_DRAW: Int
    fun glGenBuffers(): Int
    fun glBindBuffer(target: Int, buffer: Int)
    fun glBufferData(target: Int, size: Long, data: java.nio.ByteBuffer, usage: Int)

    val GL_FRAMEBUFFER: Int
    val GL_FRAMEBUFFER_COMPLETE: Int
    fun glGenFramebuffers(): Int
    fun glBindFramebuffer(target: Int, framebuffer: Int)
    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)
    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int)
    fun glDrawBuffers(bufs: IntArray)
    fun glCheckFramebufferStatus(target: Int): Int

    val GL_FLOAT: Int
    val GL_UNSIGNED_INT: Int
    val GL_UNSIGNED_BYTE: Int
    val GL_TRIANGLES: Int
    fun glEnableVertexAttribArray(index: Int)
    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long)
    fun glDisableVertexAttribArray(index: Int)
    fun glDrawElements(mode: Int, count: Int, type: Int, offset: Long)

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
    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: java.nio.ByteBuffer)

    val GL_RENDERBUFFER: Int
    val GL_DEPTH_COMPONENT24: Int
    fun glGenRenderbuffers() : Int
    fun glBindRenderbuffer(target: Int, renderbuffer: Int)
    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)

    val GL_RGB: Int
    val GL_RGBA: Int
    val GL_TEXTURE_2D: Int
    val GL_TEXTURE0: Int
    val GL_TEXTURE_MIN_FILTER: Int
    val GL_TEXTURE_MAG_FILTER: Int
    val GL_NEAREST: Int
    val GL_TEXTURE_WRAP_S: Int
    val GL_TEXTURE_WRAP_T: Int
    val GL_REPEAT: Int
    val GL_RGBA16F: Int
    val GL_RGB16F: Int
    val GL_COLOR_ATTACHMENT0: Int
    val GL_COLOR_ATTACHMENT1: Int
    val GL_COLOR_ATTACHMENT2: Int
    val GL_DEPTH_ATTACHMENT: Int
    fun glGenTextures(): Int
    fun glBindTexture(target: Int, texture: Int)
    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: java.nio.Buffer?)
    fun glTexParameteri(target: Int, pname: Int, param: Int)
    fun glActiveTexture(texture: Int)
}