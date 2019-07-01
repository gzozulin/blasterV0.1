package com.gzozulin.wallpaper.gl

import android.opengl.GLES30
import android.opengl.GLU

fun checkForGLError() {
    val errorCode = GLES30.glGetError()
    check(errorCode == GLES30.GL_NO_ERROR) {
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

fun getMaxFramebufferAttachments(): Int {
    val result = IntArray(1)
    GLES30.glGetIntegerv(GLES30.GL_MAX_COLOR_ATTACHMENTS, result, 0)
    return result[0]
}