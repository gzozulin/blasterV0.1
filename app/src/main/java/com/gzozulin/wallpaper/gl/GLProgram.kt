package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import java.lang.IllegalStateException

enum class GLShaderType(val type: Int) {
    VERTEX_SHADER(GLES20.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(GLES20.GL_FRAGMENT_SHADER)
}

enum class GLAttribute(val label: String, val size: Int) {
    ATTRIBUTE_POSITION("aPosition", 3),
    ATTRIBUTE_COLOR("aColor", 3),
}

enum class GLUniform(val label: String) {
    UNIFORM_MVP("uMvp"),
    UNIFORM_COLOR("uColor")
}

class GLShader(val type: GLShaderType, source: String) {
    val handle = GLES20.glCreateShader(type.type).also { checkForGLError() }

    init {
        glCall {
            GLES20.glShaderSource(handle, source)
            GLES20.glCompileShader(handle)
        }
        val isCompiled = IntArray(1)
        GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, isCompiled, 0)
        if (isCompiled[0] == GLES20.GL_FALSE) {
            throw IllegalStateException(GLES20.glGetShaderInfoLog(handle))
        }
    }

    fun delete() {
        glCall { GLES20.glDeleteShader(handle) }
    }
}


// todo we can check if the program is bound before sending uniforms
class GLProgram(private val vertexShader: GLShader, private val fragmentShader: GLShader) : GLBindable {
    private val handle =  GLES20.glCreateProgram().also { checkForGLError() }

    private val attribLocations = HashMap<GLAttribute, Int>()
    private val uniformLocations = HashMap<GLUniform, Int>()

    init {
        check(vertexShader.type == GLShaderType.VERTEX_SHADER)
        check(fragmentShader.type == GLShaderType.FRAGMENT_SHADER)
        glCall {
            GLES20.glAttachShader(handle, vertexShader.handle)
            GLES20.glAttachShader(handle, fragmentShader.handle)
            GLES20.glLinkProgram(handle)
        }
        val isLinked = IntArray(1)
        GLES20.glGetProgramiv(handle, GLES20.GL_LINK_STATUS, isLinked, 0)
        if (isLinked[0] == GLES20.GL_FALSE) {
            throw IllegalStateException(GLES20.glGetProgramInfoLog(handle))
        }
        cacheAttributes()
        cacheUniforms()
    }

    private fun cacheAttributes() {
        GLAttribute.values().forEach {
            val location = GLES20.glGetAttribLocation(handle, it.label).also { checkForGLError() }
            if (location != -1) {
                attribLocations[it] = location
            }
        }
    }

    private fun cacheUniforms() {
        GLUniform.values().forEach {
            val location = GLES20.glGetUniformLocation(handle, it.label).also { checkForGLError() }
            if (location != -1) {
                uniformLocations[it] = location
            }
        }
    }

    fun setAttributes(attributes: List<GLAttribute>) {
        var index = 0
        var stride = 0
        var offset = 0
        attributes.forEach { stride += it.size * 4 }
        attributes.forEach {
            GLES20.glEnableVertexAttribArray(index).also { checkForGLError() }
            GLES20.glVertexAttribPointer(attribLocations[it]!!, 3, GLES20.GL_FLOAT, false, stride, offset).also { checkForGLError() }
            index++
            offset += it.size * 4
        }
    }

    fun delete() {
        glCall { GLES20.glDeleteProgram(handle) }
        vertexShader.delete()
        fragmentShader.delete()
    }

    override fun bind(action: () -> Unit) {
        glCall { GLES20.glUseProgram(handle) }
        action.invoke()
        glCall { GLES20.glUseProgram(0) }
    }

    fun setUniform(uniform: GLUniform, value: Float) {
        glCall { GLES20.glUniform1f(uniformLocations[uniform]!!, value) }
    }

    fun setUniform(uniform: GLUniform, value: Vector3f) {
        glCall { GLES20.glUniform3fv(uniformLocations[uniform]!!, 1, value.values, 0) }
    }

    fun setUniform(uniform: GLUniform, value: Matrix4f) {
        glCall { GLES20.glUniformMatrix4fv(uniformLocations[uniform]!!, 1, false, value.values, 0) }
    }
}