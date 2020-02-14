package com.blaster.gl

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

private val bufferVec2 = ByteBuffer.allocateDirect(2 * 4)
        .order(ByteOrder.nativeOrder())

private val bufferVec3 = ByteBuffer.allocateDirect(3 * 4)
        .order(ByteOrder.nativeOrder())

private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4)
        .order(ByteOrder.nativeOrder())

enum class GlShaderType(val type: Int) {
    VERTEX_SHADER(backend.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(backend.GL_FRAGMENT_SHADER)
}

class GlShader(val type: GlShaderType, source: String) {
    val handle = glCheck { backend.glCreateShader(type.type) }

    init {
        glCheck {
            backend.glShaderSource(handle, source)
            backend.glCompileShader(handle)
        }
        val isCompiled = backend.glGetShaderi(handle, backend.GL_COMPILE_STATUS)
        if (isCompiled == backend.GL_FALSE) {
            // I am also printing the source of the shader to ease debugging and error deciphering
            val sb = StringBuffer()
            source.lines().forEachIndexed { index, line ->
                sb.append("$index $line\n")
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
// todo: we can send uniform only if changed
class GlProgram(private val vertexShader: GlShader, private val fragmentShader: GlShader) : GlBindable {
    private val handle = glCheck { backend.glCreateProgram() }

    private val uniformLocations = HashMap<GlUniform, Int>()
    private val arrayUniformLoctions = HashMap<String, Int>()

    init {
        createProgram()
    }

    private fun createProgram() {
        check(vertexShader.type == GlShaderType.VERTEX_SHADER)
        check(fragmentShader.type == GlShaderType.FRAGMENT_SHADER)
        glCheck {
            backend.glAttachShader(handle, vertexShader.handle)
            backend.glAttachShader(handle, fragmentShader.handle)
            backend.glLinkProgram(handle)
        }
        val isLinked = backend.glGetProgrami(handle, backend.GL_LINK_STATUS)
        if (isLinked == backend.GL_FALSE) {
            throw IllegalStateException(backend.glGetProgramInfoLog(handle))
        }
        cacheUniforms()
    }

    private fun cacheUniforms() {
        GlUniform.values().forEach {
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

    fun setTexture(uniform: GlUniform, texture: GlTexture) {
        setUniform(uniform, texture.unit)
    }

    fun setUniform(uniform: GlUniform, value: Matrix4f) {
        value.get(bufferMat4)
        glCheck { backend.glUniformMatrix4fv(uniformLocations[uniform]!!, 1, false, bufferMat4) }
    }

    fun setUniform(uniform: GlUniform, value: Int) {
        glCheck { backend.glUniform1i(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GlUniform, value: Float) {
        glCheck { backend.glUniform1f(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GlUniform, value: Vector2f) {
        value.get(bufferVec2)
        glCheck { backend.glUniform2fv(uniformLocations[uniform]!!, bufferVec2) }
    }

    fun setUniform(uniform: GlUniform, value: Vector3f) {
        value.get(bufferVec3)
        glCheck { backend.glUniform3fv(uniformLocations[uniform]!!, bufferVec3) }
    }

    // todo: can be improved with caching
    fun setArrayUniform(uniform: GlUniform, index: Int, value: Vector3f) {
        val label = uniform.label.format(index)
        var location: Int? = arrayUniformLoctions[label]
        if (location == null) {
            location = glCheck { backend.glGetUniformLocation(handle, label) }
            arrayUniformLoctions[label] = location
        }
        value.get(bufferVec3)
        glCheck { backend.glUniform3fv(location, bufferVec3) }
    }
}