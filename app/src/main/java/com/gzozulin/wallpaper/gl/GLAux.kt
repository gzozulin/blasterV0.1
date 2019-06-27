package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import android.opengl.GLU

fun checkForGLError() {
    val errorCode = GLES20.glGetError()
    check(errorCode == GLES20.GL_NO_ERROR) {
        "We have a GL error: ${GLU.gluErrorString(errorCode)}"
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