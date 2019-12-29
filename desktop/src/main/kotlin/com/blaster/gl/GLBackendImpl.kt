package com.blaster.gl

import org.lwjgl.opengl.*
import java.nio.Buffer
import java.nio.FloatBuffer

class GLBackendImpl : GLBackend {
    override val GL_TRUE: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_FALSE: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_NO_ERROR: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glGetError(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun gluErrorString(error: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_ARRAY_BUFFER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_ELEMENT_ARRAY_BUFFER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_STATIC_DRAW: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glGenBuffers(n: Int, buffers: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glBindBuffer(target: Int, buffer: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glBufferData(target: Int, size: Int, data: Buffer, usage: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_FRAMEBUFFER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_FRAMEBUFFER_COMPLETE: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glGenFramebuffers(n: Int, framebuffers: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glDrawBuffers(n: Int, bufs: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_FLOAT: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_UNSIGNED_INT: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TRIANGLES: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glEnableVertexAttribArray(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glDisableVertexAttribArray(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_VERTEX_SHADER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_FRAGMENT_SHADER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_COMPILE_STATUS: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_LINK_STATUS: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glCreateShader(type: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glShaderSource(shader: Int, string: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glCompileShader(shader: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glDeleteShader(shader: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glCreateProgram(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glAttachShader(program: Int, shader: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glLinkProgram(program: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glGetProgramInfoLog(program: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glDeleteProgram(program: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glUseProgram(program: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glUniform1i(location: Int, x: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glUniform1f(location: Int, x: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_RENDERBUFFER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_DEPTH_COMPONENT24: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glGenRenderbuffers(n: Int, renderbuffers: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val GL_RGBA: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE_2D: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE0: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE_MIN_FILTER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE_MAG_FILTER: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_NEAREST: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE_WRAP_S: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_TEXTURE_WRAP_T: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val GL_REPEAT: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun glGenTextures(n: Int, textures: IntArray, offset: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glBindTexture(target: Int, texture: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun glActiveTexture(texture: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}