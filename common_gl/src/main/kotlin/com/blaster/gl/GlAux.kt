package com.blaster.gl

import java.lang.Error

private val backend = GlLocator.locate()

class GlError(private val errorCode: Int) : Error() {
    override fun toString(): String {
        val msg = when (errorCode) {
            0x0   -> "GL_NO_ERROR"
            0x500 -> "GL_INVALID_ENUM"
            0x501 -> "GL_INVALID_VALUE"
            0x502 -> "GL_INVALID_OPERATION"
            0x503 -> "GL_STACK_OVERFLOW"
            0x504 -> "GL_STACK_UNDERFLOW"
            0x505 -> "GL_OUT_OF_MEMORY"
            else -> throw TODO("Unknown error code: $errorCode")
        }
        return "OpenGL error: $msg($errorCode)"
    }
}

private fun checkForGLError() {
    val errorCode = backend.glGetError()
    if (errorCode != backend.GL_NO_ERROR) {
        throw GlError(errorCode)
    }
}

fun <T> glCheck(action: () -> T): T {
    val result = action.invoke()
    checkForGLError()
    return result
}

interface GLBindable {
    fun bind()
    fun unbind()
}

fun glBind(bindable: GLBindable, action: () -> Unit) {
    bindable.bind()
    action.invoke()
    bindable.unbind()
}

// todo: remove creation of the list each frame
fun glBind(bindables: List<GLBindable>, action: () -> Unit) {
    bindables.forEach { it.bind() }
    action.invoke()
    bindables.reversed().forEach { it.unbind() }
}