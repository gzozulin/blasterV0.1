package com.blaster.gl

import com.blaster.common.color
import com.blaster.common.vec3
import java.nio.ByteBuffer

class GlLocator {
    companion object {
        private var instance: GlBackend? = null

        fun locate(): GlBackend {
            if (instance == null) {
                val clazz = Class.forName("com.blaster.gl.GlBackendImpl")
                val ctor = clazz.getConstructor()
                instance = ctor.newInstance() as GlBackend
            }
            return instance!!
        }
    }
}

interface GlBackend {
    val GL_TRUE: Int
    val GL_FALSE: Int
    val GL_NO_ERROR: Int
    val GL_FLOAT: Int
    val GL_UNSIGNED_INT: Int
    val GL_UNSIGNED_BYTE: Int
    val GL_BYTE: Int
    val GL_TRIANGLES: Int
    val GL_LINES: Int
    val GL_POINTS: Int
    val GL_DEPTH_TEST: Int
    val GL_COLOR_BUFFER_BIT: Int
    val GL_DEPTH_BUFFER_BIT: Int
    val GL_CCW: Int
    val GL_CULL_FACE: Int
    val GL_ARRAY_BUFFER: Int
    val GL_ELEMENT_ARRAY_BUFFER: Int
    val GL_STATIC_DRAW: Int
    val GL_STREAM_DRAW: Int
    val GL_FRAMEBUFFER: Int
    val GL_FRAMEBUFFER_COMPLETE: Int
    val GL_VERTEX_SHADER: Int
    val GL_FRAGMENT_SHADER: Int
    val GL_COMPILE_STATUS: Int
    val GL_LINK_STATUS: Int
    val GL_RENDERBUFFER: Int
    val GL_DEPTH_COMPONENT24: Int
    val GL_RGB: Int
    val GL_RGBA: Int
    val GL_TEXTURE_2D: Int
    val GL_TEXTURE_CUBE_MAP: Int
    val GL_TEXTURE_CUBE_MAP_POSITIVE_X: Int
    val GL_TEXTURE0: Int
    val GL_TEXTURE_MIN_FILTER: Int
    val GL_TEXTURE_MAG_FILTER: Int
    val GL_NEAREST: Int
    val GL_TEXTURE_WRAP_S: Int
    val GL_TEXTURE_WRAP_T: Int
    val GL_TEXTURE_WRAP_R: Int
    val GL_REPEAT: Int
    val GL_CLAMP_TO_EDGE: Int
    val GL_RGBA16F: Int
    val GL_RGB16F: Int
    val GL_COLOR_ATTACHMENT0: Int
    val GL_COLOR_ATTACHMENT1: Int
    val GL_COLOR_ATTACHMENT2: Int
    val GL_COLOR_ATTACHMENT3: Int
    val GL_COLOR_ATTACHMENT4: Int
    val GL_COLOR_ATTACHMENT5: Int
    val GL_DEPTH_ATTACHMENT: Int
    val GL_MAP_UNSYNCHRONIZED_BIT: Int
    val GL_MAP_WRITE_BIT: Int
    val GL_READ_ONLY: Int
    val GL_WRITE_ONLY: Int
    val GL_READ_WRITE: Int
    val GL_BLEND: Int
    val GL_SRC_ALPHA: Int
    val GL_LEQUAL: Int
    val GL_ONE_MINUS_SRC_ALPHA: Int
    val GL_MODELVIEW: Int
    val GL_PROJECTION: Int

    fun glGetError(): Int
    fun gluErrorString(error: Int): String
    fun glEnable(cap: Int)
    fun glDisable(cap: Int)
    fun glDepthFunc(func: Int)
    fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun glViewport(x: Int, y: Int, width: Int, height: Int)
    fun glFrontFace(mode: Int)
    fun glClear(mask: Int)
    fun glGenBuffers(): Int
    fun glBindBuffer(target: Int, buffer: Int)
    fun glBufferData(target: Int, data: ByteBuffer, usage: Int)
    fun glGenFramebuffers(): Int
    fun glBindFramebuffer(target: Int, framebuffer: Int)
    fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)
    fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int)
    fun glDrawBuffers(bufs: IntArray)
    fun glCheckFramebufferStatus(target: Int): Int
    fun glEnableVertexAttribArray(index: Int)
    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long)
    fun glVertexAttribDivisor(indx: Int, divisor: Int)
    fun glDisableVertexAttribArray(index: Int)
    fun glDrawElements(mode: Int, count: Int, type: Int, offset: Long)
    fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, offset: Long, instances: Int)
    fun glCreateShader(type: Int): Int
    fun glShaderSource(shader: Int, string: String)
    fun glCompileShader(shader: Int)
    fun glGetShaderi(shader: Int, pname: Int): Int
    fun glGetShaderInfoLog(shader: Int): String
    fun glDeleteShader(shader: Int)
    fun glCreateProgram(): Int
    fun glAttachShader(program: Int, shader: Int)
    fun glLinkProgram(program: Int)
    fun glGetProgrami(program: Int, pname: Int): Int
    fun glGetProgramInfoLog(program: Int): String
    fun glGetUniformLocation(program: Int, name: String): Int
    fun glDeleteProgram(program: Int)
    fun glUseProgram(program: Int)
    fun glUniform1i(location: Int, x: Int)
    fun glUniform1f(location: Int, x: Float)
    fun glUniform2fv(location: Int, v: ByteBuffer)
    fun glUniform3fv(location: Int, v: ByteBuffer)
    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: ByteBuffer)
    fun glGenRenderbuffers() : Int
    fun glBindRenderbuffer(target: Int, renderbuffer: Int)
    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    fun glGenTextures(): Int
    fun glBindTexture(target: Int, texture: Int)
    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ByteBuffer?)
    fun glTexParameteri(target: Int, pname: Int, param: Int)
    fun glActiveTexture(texture: Int)
    fun glMapBuffer(target: Int, access: Int, oldBuffer: ByteBuffer): ByteBuffer
    fun glUnapBuffer(target: Int): Boolean
    fun glMapBufferRange(target: Int, offset: Long, length: Long, access: Int, oldBuffer: ByteBuffer): ByteBuffer
    fun glBegin(mode: Int)
    fun glEnd()
    fun glColor3f(rgb: color)
    fun glColor3f(r: Float, g: Float, b: Float)
    fun glVertex3f(x: Float, y: Float, z: Float)
    fun glVertex3f(xyz: vec3)
    fun glBlendFunc(sfactor: Int, dfactor: Int)
    fun glMatrixMode(mode: Int)
    fun glLoadMatrix(matrix: ByteBuffer)
}