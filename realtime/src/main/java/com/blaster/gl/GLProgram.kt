package com.blaster.gl

import org.joml.Matrix4f
import org.joml.Vector3f
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GLBackendLocator.instance()

private val bufferVec3 = ByteBuffer.allocateDirect(4 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

enum class GLShaderType(val type: Int) {
    VERTEX_SHADER(backend.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(backend.GL_FRAGMENT_SHADER)
}

class GLShader(val type: GLShaderType, source: String) {
    val handle = glCheck { backend.glCreateShader(type.type) }

    init {
        glCheck {
            backend.glShaderSource(handle, source)
            backend.glCompileShader(handle)
        }
        val isCompiled = IntArray(1)
        backend.glGetShaderiv(handle, backend.GL_COMPILE_STATUS, isCompiled, 0)
        if (isCompiled[0] == backend.GL_FALSE) {
            var index = 1
            val sb = StringBuffer()
            source.lines().forEach {
                sb.append("$index $it\n")
                index++
            }
            val reason = "Failed to compile shader:\n\n$sb\n\nWith reason:\n\n${backend.glGetShaderInfoLog(handle)}"
            throw IllegalStateException(reason)
        }
    }

    fun delete() {
        glCheck { backend.glDeleteShader(handle) }
    }
}

// todo use explicit locations for uniforms
// todo we can check if the program is bound before sending uniforms
class GLProgram(private val vertexShader: GLShader, private val fragmentShader: GLShader) : GLBindable {
    private val handle = glCheck { backend.glCreateProgram() }

    private val uniformLocations = HashMap<GLUniform, Int>()

    init {
        check(vertexShader.type == GLShaderType.VERTEX_SHADER)
        check(fragmentShader.type == GLShaderType.FRAGMENT_SHADER)
        glCheck {
            backend.glAttachShader(handle, vertexShader.handle)
            backend.glAttachShader(handle, fragmentShader.handle)
            backend.glLinkProgram(handle)
        }
        val isLinked = IntArray(1)
        backend.glGetProgramiv(handle, backend.GL_LINK_STATUS, isLinked, 0)
        if (isLinked[0] == backend.GL_FALSE) {
            throw IllegalStateException(backend.glGetProgramInfoLog(handle))
        }
        cacheUniforms()
    }

    private fun cacheUniforms() {
        GLUniform.values().forEach {
            val location = glCheck { backend.glGetUniformLocation(handle, it.label) }
            if (location != -1) {
                uniformLocations[it] = location
            }
        }
    }

    fun delete() {
        glCheck { backend.glDeleteProgram(handle) }
        vertexShader.delete()
        fragmentShader.delete()
    }

    override fun bind() {
        glCheck { backend.glUseProgram(handle) }
    }

    override fun unbind() {
        glCheck { backend.glUseProgram(0) }
    }

    fun setTexture(uniform: GLUniform, texture: GLTexture) {
        setUniform(uniform, texture.unit)
    }

    private fun setUniform(uniform: GLUniform, value: Int) {
        glCheck { backend.glUniform1i(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GLUniform, value: Float) {
        glCheck { backend.glUniform1f(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GLUniform, value: Vector3f) {
        glCheck { backend.glUniform3fv(uniformLocations[uniform]!!, 1, floatArrayOf(value.x, value.y, value.z), 0) }
    }

    fun setUniform(uniform: GLUniform, value: Matrix4f) {
        glCheck { backend.glUniformMatrix4fv(uniformLocations[uniform]!!, 1, false, bufferMat4) }
    }
}