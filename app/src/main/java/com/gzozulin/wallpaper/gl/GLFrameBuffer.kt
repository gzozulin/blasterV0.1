package com.gzozulin.wallpaper.gl

import android.opengl.GLES20

class GLFrameBuffer(private val target: Int) : GLBindable {
    private val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES20.glGenFramebuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    override fun bind() {
        glCheck { GLES20.glBindFramebuffer(target, handle) }
    }

    override fun unbind() {
        glCheck { GLES20.glBindFramebuffer(target, 0) }
    }

    fun setTexture(attachement: Int, texture: GLTexture, level: Int = 0) {
        GLES20.glFramebufferTexture2D(target, attachement, texture.target, texture.handle, 0) // 0 - level
    }
}