package com.blaster.gl

import android.opengl.GLES30
import org.joml.Matrix4f
import org.joml.Vector3f
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class GLShaderType(val type: Int) {
    VERTEX_SHADER(GLES30.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(GLES30.GL_FRAGMENT_SHADER)
}

class GLShader(val type: GLShaderType, source: String) {
    val handle = glCheck { GLES30.glCreateShader(type.type) }

    init {
        glCheck {
            GLES30.glShaderSource(handle, source)
            GLES30.glCompileShader(handle)
        }
        val isCompiled = IntArray(1)
        GLES30.glGetShaderiv(handle, GLES30.GL_COMPILE_STATUS, isCompiled, 0)
        if (isCompiled[0] == GLES30.GL_FALSE) {
            var index = 1
            val sb = StringBuffer()
            source.lines().forEach {
                sb.append("$index $it\n")
                index++
            }
            val reason = "Failed to compile shader:\n\n$sb\n\nWith reason:\n\n${GLES30.glGetShaderInfoLog(handle)}"
            throw IllegalStateException(reason)
        }
    }

    fun delete() {
        glCheck { GLES30.glDeleteShader(handle) }
    }
}

// todo use explicit locations for uniforms
// todo we can check if the program is bound before sending uniforms
class GLProgram(private val vertexShader: GLShader, private val fragmentShader: GLShader) : GLBindable {
    private val handle = glCheck { GLES30.glCreateProgram() }

    private val uniformLocations = HashMap<GLUniform, Int>()

    private val bufferVec3 = ByteBuffer.allocateDirect(4 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()

    init {
        check(vertexShader.type == GLShaderType.VERTEX_SHADER)
        check(fragmentShader.type == GLShaderType.FRAGMENT_SHADER)
        glCheck {
            GLES30.glAttachShader(handle, vertexShader.handle)
            GLES30.glAttachShader(handle, fragmentShader.handle)
            GLES30.glLinkProgram(handle)
        }
        val isLinked = IntArray(1)
        GLES30.glGetProgramiv(handle, GLES30.GL_LINK_STATUS, isLinked, 0)
        if (isLinked[0] == GLES30.GL_FALSE) {
            throw IllegalStateException(GLES30.glGetProgramInfoLog(handle))
        }
        cacheUniforms()
    }

    private fun cacheUniforms() {
        GLUniform.values().forEach {
            val location = glCheck { GLES30.glGetUniformLocation(handle, it.label) }
            if (location != -1) {
                uniformLocations[it] = location
            }
        }
    }

    fun delete() {
        glCheck { GLES30.glDeleteProgram(handle) }
        vertexShader.delete()
        fragmentShader.delete()
    }

    override fun bind() {
        glCheck { GLES30.glUseProgram(handle) }
    }

    override fun unbind() {
        glCheck { GLES30.glUseProgram(0) }
    }

    fun setTexture(uniform: GLUniform, texture: GLTexture) {
        setUniform(uniform, texture.unit)
    }

    private fun setUniform(uniform: GLUniform, value: Int) {
        glCheck { GLES30.glUniform1i(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GLUniform, value: Float) {
        glCheck { GLES30.glUniform1f(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GLUniform, value: Vector3f) {
        glCheck { GLES30.glUniform3fv(uniformLocations[uniform]!!, 1, floatArrayOf(value.x, value.y, value.z), 0) }
    }

    fun setUniform(uniform: GLUniform, value: Matrix4f) {
        glCheck { GLES30.glUniformMatrix4fv(uniformLocations[uniform]!!, 1, false, bufferMat4) }
    }
}