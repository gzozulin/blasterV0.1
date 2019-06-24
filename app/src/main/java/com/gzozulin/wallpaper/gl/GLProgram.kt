package com.gzozulin.wallpaper.gl

import android.opengl.GLES20

enum class ShaderType(val type: Int) {
    VERTEX_SHADER(GLES20.GL_VERTEX_SHADER),
    FRAGMENT_SHADER(GLES20.GL_FRAGMENT_SHADER)
}

enum class ShaderAttribute(val label: String) {
    ATTRIBUTE_POSITION("vPosition")
}

enum class ShaderUniform(val label: String) {
    UNIFORM_MVP("uMvp"),
    UNIFORM_COLOR("uColor")
}

class GLShader(val type: ShaderType, source: String) {
    val handle = GLES20.glCreateShader(type.type).also { checkForGLError() }

    init {
        GLES20.glShaderSource(handle, source).also { checkForGLError() }
        GLES20.glCompileShader(handle).also { checkForGLError() }
    }

    fun delete() {
        GLES20.glDeleteShader(handle).also { checkForGLError() }
    }
}

class GLProgram(vertexShader: GLShader, fragmentShader: GLShader) {
    private val handle =  GLES20.glCreateProgram().also { checkForGLError() }

    private val attributes = HashMap<ShaderAttribute, Int>()
    private val uniforms = HashMap<ShaderUniform, Int>()

    init {
        check(vertexShader.type == ShaderType.VERTEX_SHADER)
        check(fragmentShader.type == ShaderType.FRAGMENT_SHADER)
        GLES20.glAttachShader(handle, vertexShader.handle).also { checkForGLError() }
        GLES20.glAttachShader(handle, fragmentShader.handle).also { checkForGLError() }
        GLES20.glLinkProgram(handle).also { checkForGLError() }
        cacheAttributes()
        cacheUniforms()
    }

    private fun cacheAttributes() {
        ShaderAttribute.values().forEach {
            val location = GLES20.glGetAttribLocation(handle, it.label)
            if (location != -1) {
                attributes[it] = location
            }
        }
    }

    private fun cacheUniforms() {
        ShaderUniform.values().forEach {
            val location = GLES20.glGetUniformLocation(handle, it.label)
            if (location != -1) {
                uniforms[it] = location
            }
        }
    }

    fun delete() {
        GLES20.glDeleteProgram(handle).also { checkForGLError() }
    }

    fun bind() {
        GLES20.glUseProgram(handle).also { checkForGLError() }
    }

    fun sendUniform(uniform: ShaderUniform, value: Float) {
        GLES20.glUniform1f(uniforms[uniform]!!, value).also { checkForGLError() }
    }

    fun sendUniform(uniform: ShaderUniform, value: Vector4f) {
        GLES20.glUniform4fv(uniforms[uniform]!!, 1, value.values, 0).also { checkForGLError() }
    }

    fun sendUniform(uniform: ShaderUniform, value: Matrix4f) {
        GLES20.glUniformMatrix4fv(uniforms[uniform]!!, 1, false, value.values, 0).also { checkForGLError() }
    }
}