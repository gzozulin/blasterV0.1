package com.blaster.gl

private val backend = GLLocator.instance()

fun checkForGLError() {
    val errorCode = backend.glGetError()
    check(errorCode == backend.GL_NO_ERROR) {
        "We have a GL error: ${backend.gluErrorString(errorCode)}"
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

fun glBind(bindables: List<GLBindable>, action: () -> Unit) {
    bindables.forEach { it.bind() }
    action.invoke()
    bindables.reversed().forEach { it.unbind() }
}