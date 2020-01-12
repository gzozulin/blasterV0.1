package com.blaster.gl

import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GlBackendImpl : GlBackend {
    override val GL_NO_ERROR:           Int get() = GL11.GL_NO_ERROR
    override val GL_TRUE:               Int get() = GL11.GL_TRUE
    override val GL_FALSE:              Int get() = GL11.GL_FALSE
    override val GL_DEPTH_TEST:         Int get() = GL11.GL_DEPTH_TEST
    override val GL_COLOR_BUFFER_BIT:   Int get() = GL11.GL_COLOR_BUFFER_BIT
    override val GL_DEPTH_BUFFER_BIT:   Int get() = GL11.GL_DEPTH_BUFFER_BIT
    override val GL_CCW:                Int get() = GL11.GL_CCW
    override val GL_CULL_FACE:          Int get() = GL11.GL_CULL_FACE
    override val GL_ARRAY_BUFFER:       Int get() = GL15.GL_ARRAY_BUFFER
    override val GL_ELEMENT_ARRAY_BUFFER: Int get() = GL15.GL_ELEMENT_ARRAY_BUFFER
    override val GL_STATIC_DRAW:        Int get() = GL15.GL_STATIC_DRAW
    override val GL_STREAM_DRAW:        Int get() = GL15.GL_STREAM_DRAW
    override val GL_FRAMEBUFFER:        Int get() = ARBFramebufferObject.GL_FRAMEBUFFER
    override val GL_FRAMEBUFFER_COMPLETE: Int get() = ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE
    override val GL_FLOAT:              Int get() = GL11.GL_FLOAT
    override val GL_UNSIGNED_INT:       Int get() = GL11.GL_UNSIGNED_INT
    override val GL_UNSIGNED_BYTE:      Int get() = GL11.GL_UNSIGNED_BYTE
    override val GL_BYTE:               Int get() = GL11.GL_BYTE
    override val GL_TRIANGLES:          Int get() = GL11.GL_TRIANGLES
    override val GL_POINTS:             Int get() = GL11.GL_POINTS
    override val GL_VERTEX_SHADER:      Int get() = GL20.GL_VERTEX_SHADER
    override val GL_FRAGMENT_SHADER:    Int get() = GL20.GL_FRAGMENT_SHADER
    override val GL_COMPILE_STATUS:     Int get() = GL20.GL_COMPILE_STATUS
    override val GL_LINK_STATUS:        Int get() = GL20.GL_LINK_STATUS
    override val GL_RENDERBUFFER:       Int get() = GL30.GL_RENDERBUFFER
    override val GL_DEPTH_COMPONENT24:  Int get() = GL14.GL_DEPTH_COMPONENT24
    override val GL_RGB:                Int get() = GL11.GL_RGB
    override val GL_RGBA:               Int get() = GL11.GL_RGBA
    override val GL_TEXTURE_2D:         Int get() = GL11.GL_TEXTURE_2D
    override val GL_TEXTURE0:           Int get() = GL13.GL_TEXTURE0
    override val GL_TEXTURE_MIN_FILTER: Int get() = GL11.GL_TEXTURE_MIN_FILTER
    override val GL_TEXTURE_MAG_FILTER: Int get() = GL11.GL_TEXTURE_MAG_FILTER
    override val GL_NEAREST:            Int get() = GL11.GL_NEAREST
    override val GL_TEXTURE_WRAP_S:     Int get() = GL11.GL_TEXTURE_WRAP_S
    override val GL_TEXTURE_WRAP_T:     Int get() = GL11.GL_TEXTURE_WRAP_T
    override val GL_REPEAT:             Int get() = GL11.GL_REPEAT
    override val GL_RGBA16F:            Int get() = GL30.GL_RGBA16F
    override val GL_RGB16F:             Int get() = GL30.GL_RGB16F
    override val GL_COLOR_ATTACHMENT0:  Int get() = GL30.GL_COLOR_ATTACHMENT0
    override val GL_COLOR_ATTACHMENT1:  Int get() = GL30.GL_COLOR_ATTACHMENT1
    override val GL_COLOR_ATTACHMENT2:  Int get() = GL30.GL_COLOR_ATTACHMENT2
    override val GL_DEPTH_ATTACHMENT:   Int get() = GL30.GL_DEPTH_ATTACHMENT
    override val GL_MAP_WRITE_BIT:      Int get() = GL30.GL_MAP_WRITE_BIT
    override val GL_MAP_UNSYNCHRONIZED_BIT: Int get() = GL30.GL_MAP_UNSYNCHRONIZED_BIT
    override val GL_READ_ONLY:          Int get() = GL15.GL_READ_ONLY
    override val GL_READ_WRITE:         Int get() = GL15.GL_READ_WRITE
    override val GL_WRITE_ONLY:         Int get() = GL15.GL_READ_WRITE

    override fun glGetError() = GL11.glGetError()
    override fun gluErrorString(error: Int) = "Undeciphered error $error"
    override fun glEnable(cap: Int) = GL11.glEnable(cap)
    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)  =
            GL11.glClearColor(red, green, blue, alpha)
    override fun glViewport(x: Int, y: Int, width: Int, height: Int) =
            GL11.glViewport(x, y, width, height)
    override fun glFrontFace(mode: Int) = GL11.glFrontFace(mode)
    override fun glClear(mask: Int) = GL11.glClear(mask)
    override fun glGenBuffers(): Int = GL15.glGenBuffers()
    override fun glBindBuffer(target: Int, buffer: Int) = GL15.glBindBuffer(target, buffer)
    override fun glBufferData(target: Int, data: ByteBuffer, usage: Int) =
            GL15.glBufferData(target, data, usage)
    override fun glGenFramebuffers() = ARBFramebufferObject.glGenFramebuffers()
    override fun glBindFramebuffer(target: Int, framebuffer: Int) =
            ARBFramebufferObject.glBindFramebuffer(target, framebuffer)
    override fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) =
            ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    override fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) =
            ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    override fun glDrawBuffers(bufs: IntArray) {
        // todo: remove allocation
        val buffer = ByteBuffer.allocateDirect(bufs.size * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
        buffer.put(bufs).position(0)
        GL20.glDrawBuffers(buffer)
    }
    override fun glCheckFramebufferStatus(target: Int): Int =
            ARBFramebufferObject.glCheckFramebufferStatus(target)
    override fun glEnableVertexAttribArray(index: Int) =
            GL20.glEnableVertexAttribArray(index)
    override fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Long) =
            GL20.glVertexAttribPointer(indx, size, type, normalized, stride, offset)

    override fun glVertexAttribDivisor(indx: Int, divisor: Int) = GL33.glVertexAttribDivisor(indx, divisor)
    override fun glDisableVertexAttribArray(index: Int) = GL20.glDisableVertexAttribArray(index)
    override fun glDrawElements(mode: Int, count: Int, type: Int, offset: Long) =
            GL11.glDrawElements(mode, count, type, offset)

    override fun glDrawElementsInstanced(mode: Int, count: Int, type: Int, offset: Long, instances: Int) =
            GL31.glDrawElementsInstanced(mode, count, type, offset, instances)
    override fun glCreateShader(type: Int): Int = GL20.glCreateShader(type)
    override fun glShaderSource(shader: Int, string: String) = GL20.glShaderSource(shader, string)
    override fun glCompileShader(shader: Int) = GL20.glCompileShader(shader)
    override fun glGetShaderi(shader: Int, pname: Int): Int = GL20.glGetShaderi(shader, pname)
    override fun glGetShaderInfoLog(shader: Int) = GL20.glGetShaderInfoLog(shader)
    override fun glDeleteShader(shader: Int) = GL20.glDeleteShader(shader)
    override fun glCreateProgram() = GL20.glCreateProgram()
    override fun glAttachShader(program: Int, shader: Int) = GL20.glAttachShader(program, shader)
    override fun glLinkProgram(program: Int) = GL20.glLinkProgram(program)
    override fun glGetProgrami(program: Int, pname: Int) = GL20.glGetProgrami(program, pname)
    override fun glGetProgramInfoLog(program: Int) = GL20.glGetProgramInfoLog(program)
    override fun glGetUniformLocation(program: Int, name: String) = GL20.glGetUniformLocation(program, name)
    override fun glDeleteProgram(program: Int) = GL20.glDeleteProgram(program)
    override fun glUseProgram(program: Int) = GL20.glUseProgram(program)
    override fun glUniform1i(location: Int, x: Int) = GL20.glUniform1i(location, x)
    override fun glUniform1f(location: Int, x: Float) = GL20.glUniform1f(location, x)
    override fun glUniform2fv(location: Int, v: ByteBuffer) =
            GL20.glUniform2fv(location, v.asFloatBuffer())
    override fun glUniform3fv(location: Int, v: ByteBuffer) =
            GL20.glUniform3fv(location, v.asFloatBuffer())
    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: ByteBuffer) =
            GL20.glUniformMatrix4fv(location, count, transpose, value)
    override fun glGenRenderbuffers() = GL30.glGenRenderbuffers()
    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) =
            GL30.glBindRenderbuffer(target, renderbuffer)
    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) =
            GL30.glRenderbufferStorage(target, internalformat, width, height)
    override fun glGenTextures() = GL11.glGenTextures()
    override fun glBindTexture(target: Int, texture: Int) = GL11.glBindTexture(target, texture)
    override fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ByteBuffer?) =
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)
    override fun glTexParameteri(target: Int, pname: Int, param: Int) = GL11.glTexParameteri(target, pname, param)
    override fun glActiveTexture(texture: Int) = GL13.glActiveTexture(texture)
    override fun glMapBuffer(target: Int, access: Int, oldBuffer: ByteBuffer) =
            GL15.glMapBuffer(target, access, oldBuffer)
    override fun glUnapBuffer(target: Int) = GL15.glUnmapBuffer(target)
    override fun glMapBufferRange(target: Int, offset: Long, length: Long, access: Int, oldBuffer: ByteBuffer): ByteBuffer =
            GL30.glMapBufferRange(target, offset, length, access, oldBuffer)
}