package com.gzozulin.wallpaper.gl

import android.opengl.GLES30

class GLFrameBuffer : GLBindable {
    private val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES30.glGenFramebuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    override fun bind() {
        glCheck { GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, handle) }
    }

    override fun unbind() {
        glCheck { GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0) }
    }

    fun setTexture(attachement: Int, texture: GLTexture) {
        glCheck { GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, attachement, texture.target, texture.handle, 0) } // 0 - level
    }

    fun setRenderBuffer(attachement: Int, renderBuffer: GLRenderBuffer) {
        glCheck { GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, attachement, GLES30.GL_RENDERBUFFER, renderBuffer.handle) }
    }

    fun setOutputs(outputs: IntArray) {
        glCheck { GLES30.glDrawBuffers(outputs.size, outputs, 0) }
    }

    fun checkIsComplete() {
        check(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) == GLES30.GL_FRAMEBUFFER_COMPLETE)
    }
}