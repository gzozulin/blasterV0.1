package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import android.opengl.GLU

fun checkForGLError() {
    val errorCode = GLES20.glGetError()
    check(errorCode == GLES20.GL_NO_ERROR) {
        "We have a GL error: ${GLU.gluErrorString(errorCode)}"
    }
}

fun glCheck(action: () -> Unit) {
    action.invoke().also { checkForGLError() }
}