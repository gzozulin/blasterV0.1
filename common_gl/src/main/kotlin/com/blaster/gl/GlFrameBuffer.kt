package com.blaster.gl

private val backend = GlLocator.locate()

class GlFrameBuffer : GlBindable {
    private var handle: Int? = null

    init {
        handle = glCheck { backend.glGenFramebuffers() }
        check(handle!! > 0)
    }

    fun free() {
        glCheck { backend.glDeleteFramebuffers(handle!!) }
        handle = null
    }

    override fun bind() {
        glCheck { backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, handle!!) }
    }

    override fun unbind() {
        glCheck { backend.glBindFramebuffer(backend.GL_FRAMEBUFFER, 0) }
    }

    fun setTexture(attachement: Int, texture: GlTexture) {
        glCheck { backend.glFramebufferTexture2D(backend.GL_FRAMEBUFFER, attachement, texture.target, texture.handle!!, 0) } // 0 - level
    }

    fun setRenderBuffer(attachement: Int, renderBuffer: GlRenderBuffer) {
        glCheck { backend.glFramebufferRenderbuffer(backend.GL_FRAMEBUFFER, attachement, backend.GL_RENDERBUFFER, renderBuffer.handle!!) }
    }

    fun setOutputs(outputs: IntArray) {
        glCheck { backend.glDrawBuffers(outputs) }
    }

    fun checkIsComplete() {
        check(backend.glCheckFramebufferStatus(backend.GL_FRAMEBUFFER) == backend.GL_FRAMEBUFFER_COMPLETE)
    }
}