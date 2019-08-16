package com.gzozulin.wallpaper.gl

import android.opengl.GLES30

class GLRenderBuffer(
        private val component: Int = GLES30.GL_DEPTH_COMPONENT24,
        private val width: Int, private val height: Int) : GLBindable {

    val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES30.glGenRenderbuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck { GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, handle) }
        glCheck { GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, component, width, height) }
        glCheck { GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0) }
    }

    override fun bind() {
        glCheck { GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, handle) }
    }

    override fun unbind() {
        glCheck { GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0) }
    }
}
