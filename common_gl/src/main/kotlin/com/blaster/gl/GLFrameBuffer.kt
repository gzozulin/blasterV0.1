package com.blaster.gl

private val backend = GLBackendLocator.instance()

class GLFrameBuffer : GLBindable {
    private val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { backend.glGenFramebuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    override fun bind() {
        glCheck { backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, handle) }
    }

    override fun unbind() {
        glCheck { backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, 0) }
    }

    fun setTexture(attachement: Int, texture: GLTexture) {
        glCheck { backend.glFramebufferTexture2D(backend.GL_FRAMEBUFFER, attachement, texture.target, texture.handle, 0) } // 0 - level
    }

    fun setRenderBuffer(attachement: Int, renderBuffer: GLRenderBuffer) {
        glCheck { backend.glFramebufferRenderbuffer(backend.GL_FRAMEBUFFER, attachement, backend.GL_RENDERBUFFER, renderBuffer.handle) }
    }

    fun setOutputs(outputs: IntArray) {
        glCheck { backend.glDrawBuffers(outputs.size, outputs, 0) }
    }

    fun checkIsComplete() {
        check(backend.glCheckFramebufferStatus(backend.GL_FRAMEBUFFER) == backend.GL_FRAMEBUFFER_COMPLETE)
    }
}