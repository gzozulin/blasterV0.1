package com.blaster.gl

import android.opengl.GLES30
import android.opengl.GLU
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class GLBackendImpl : GLBackend {
    override val GL_NO_ERROR: Int
        get() = GLES30.GL_NO_ERROR

    override fun gluErrorString(error: Int) = GLU.gluErrorString(error)
    override fun glGetError() = GLES30.glGetError()

    override val GL_DEPTH_TEST: Int
        get() = GLES30.GL_DEPTH_TEST
    override val GL_COLOR_BUFFER_BIT: Int
        get() = GLES30.GL_COLOR_BUFFER_BIT
    override val GL_DEPTH_BUFFER_BIT: Int
        get() = GLES30.GL_DEPTH_BUFFER_BIT
    override val GL_CCW: Int
        get() = GLES30.GL_CCW
    override val GL_CULL_FACE: Int
        get() = GLES30.GL_CULL_FACE

    override fun glEnable(cap: Int) = GLES30.glEnable(cap)
    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) =
            GLES30.glClearColor(red, green, blue, alpha)
    override fun glViewport(x: Int, y: Int, width: Int, height: Int) =
            GLES30.glViewport(x, y, width, height)
    override fun glFrontFace(mode: Int) = GLES30.glFrontFace(mode)
    override fun glClear(mask: Int) = GLES30.glClear(mask)

    override val GL_ARRAY_BUFFER: Int
        get() = GLES30.GL_ARRAY_BUFFER
    override val GL_ELEMENT_ARRAY_BUFFER: Int
        get() = GLES30.GL_ELEMENT_ARRAY_BUFFER
    override val GL_STATIC_DRAW: Int
        get() = GLES30.GL_STATIC_DRAW

    override fun glGenBuffers(): Int {
        val handles = IntArray(1)
        GLES30.glGenBuffers(1, handles, 0)
        return handles[0]
    }

    override fun glBindBuffer(target: Int, buffer: Int) = GLES30.glBindBuffer(target, buffer)
    override fun glBufferData(target: Int, size: Long, data: ByteBuffer, usage: Int) =
            GLES30.glBufferData(target, size.toInt(), data, usage)

    override val GL_TRUE: Int
        get() = GLES30.GL_TRUE
    override val GL_FALSE: Int
        get() = GLES30.GL_FALSE
    override val GL_FRAMEBUFFER: Int
        get() = GLES30.GL_FRAMEBUFFER
    override val GL_FRAMEBUFFER_COMPLETE: Int
        get() = GLES30.GL_FRAMEBUFFER_COMPLETE

    override fun glGenFramebuffers(): Int {
        val handles = IntArray(1)
        GLES30.glGenFramebuffers(1, handles, 0)
        return handles[0]
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) =
            GLES30.glBindFramebuffer(target, framebuffer)
    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) =
            GLES30.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) =
            GLES30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    override fun glDrawBuffers(bufs: IntArray) = GLES30.glDrawBuffers(bufs.size, bufs, 0)

    override fun glCheckFramebufferStatus(target: Int): Int =
            GLES30.glCheckFramebufferStatus(target)

    override val GL_FLOAT: Int
        get() = GLES30.GL_FLOAT
    override val GL_UNSIGNED_INT: Int
        get() = GLES30.GL_UNSIGNED_INT
    override val GL_TRIANGLES: Int
        get() = GLES30.GL_TRIANGLES

    override fun glEnableVertexAttribArray(index: Int) = GLES30.glEnableVertexAttribArray(index)
    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) =
            GLES30.glVertexAttribPointer(indx, size, type, normalized, stride, offset.toInt())
    override fun glDisableVertexAttribArray(index: Int) = GLES30.glDisableVertexAttribArray(index)
    override fun glDrawElements(mode: Int, count: Int, type: Int, offset: Long) =
            GLES30.glDrawElements(mode, count, type, offset.toInt())

    override val GL_VERTEX_SHADER: Int
        get() = GLES30.GL_VERTEX_SHADER
    override val GL_FRAGMENT_SHADER: Int
        get() = GLES30.GL_FRAGMENT_SHADER
    override val GL_COMPILE_STATUS: Int
        get() = GLES30.GL_COMPILE_STATUS
    override val GL_LINK_STATUS: Int
        get() = GLES30.GL_LINK_STATUS

    override fun glCreateShader(type: Int): Int = GLES30.glCreateShader(type)
    override fun glShaderSource(shader: Int, string: String) = GLES30.glShaderSource(shader, string)
    override fun glCompileShader(shader: Int) = GLES30.glCompileShader(shader)
    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray, offset: Int) =
            GLES30.glGetShaderiv(shader, pname, params, offset)
    override fun glGetShaderInfoLog(shader: Int): String = GLES30.glGetShaderInfoLog(shader)
    override fun glDeleteShader(shader: Int) = GLES30.glDeleteShader(shader)
    override fun glCreateProgram(): Int = GLES30.glCreateProgram()
    override fun glAttachShader(program: Int, shader: Int) = GLES30.glAttachShader(program, shader)
    override fun glLinkProgram(program: Int) = GLES30.glLinkProgram(program)
    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray, offset: Int) =
            GLES30.glGetProgramiv(program, pname, params, offset)
    override fun glGetProgramInfoLog(program: Int): String = GLES30.glGetProgramInfoLog(program)
    override fun glGetUniformLocation(program: Int, name: String): Int =
            GLES30.glGetUniformLocation(program, name)
    override fun glDeleteProgram(program: Int) = GLES30.glDeleteProgram(program)
    override fun glUseProgram(program: Int) = GLES30.glUseProgram(program)
    override fun glUniform1i(location: Int, x: Int) = GLES30. glUniform1i(location, x)
    override fun glUniform1f(location: Int, x: Float) = GLES30.glUniform1f(location, x)
    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) =
            GLES30.glUniform3fv(location, count, v, offset)
    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: ByteBuffer) =
            GLES30.glUniformMatrix4fv(location, count, transpose, value.asFloatBuffer())

    override val GL_RENDERBUFFER: Int
        get() = GLES30.GL_RENDERBUFFER
    override val GL_DEPTH_COMPONENT24: Int
        get() = GLES30.GL_DEPTH_COMPONENT24

    override fun glGenRenderbuffers(): Int {
        val handles = IntArray(1)
        GLES30.glGenRenderbuffers(1, handles, 0)
        return handles[0]
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) =
            GLES30.glBindRenderbuffer(target, renderbuffer)
    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
            GLES30.glRenderbufferStorage(target, internalformat, width, height)

    override val GL_RGBA: Int
        get() = GLES30.GL_RGBA
    override val GL_TEXTURE_2D: Int
        get() = GLES30.GL_TEXTURE_2D
    override val GL_TEXTURE0: Int
        get() = GLES30.GL_TEXTURE0
    override val GL_TEXTURE_MIN_FILTER: Int
        get() = GLES30.GL_TEXTURE_MIN_FILTER
    override val GL_TEXTURE_MAG_FILTER: Int
        get() = GLES30.GL_TEXTURE_MAG_FILTER
    override val GL_NEAREST: Int
        get() = GLES30.GL_NEAREST
    override val GL_TEXTURE_WRAP_S: Int
        get() = GLES30.GL_TEXTURE_WRAP_S
    override val GL_TEXTURE_WRAP_T: Int
        get() = GLES30.GL_TEXTURE_WRAP_T
    override val GL_REPEAT: Int
        get() = GLES30.GL_REPEAT

    override val GL_UNSIGNED_BYTE: Int
        get() = GLES30.GL_UNSIGNED_BYTE
    override val GL_RGB: Int
        get() = GLES30.GL_RGB
    override val GL_RGBA16F: Int
        get() = GLES30.GL_RGBA16F
    override val GL_RGB16F: Int
        get() = GLES30.GL_RGB16F
    override val GL_COLOR_ATTACHMENT0: Int
        get() = GLES30.GL_COLOR_ATTACHMENT0
    override val GL_COLOR_ATTACHMENT1: Int
        get() = GLES30.GL_COLOR_ATTACHMENT1
    override val GL_COLOR_ATTACHMENT2: Int
        get() = GLES30.GL_COLOR_ATTACHMENT2
    override val GL_DEPTH_ATTACHMENT: Int
        get() = GLES30.GL_DEPTH_ATTACHMENT

    override fun glGenTextures(): Int {
        val handles = IntArray(1)
        GLES30.glGenTextures(1, handles, 0)
        return handles[0]
    }

    override fun glBindTexture(target: Int, texture: Int) = GLES30.glBindTexture(target, texture)
    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer?) =
            GLES30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) =
            GLES30.glTexParameteri(target, pname, param)
    override fun glActiveTexture(texture: Int) = GLES30.glActiveTexture(texture)
}