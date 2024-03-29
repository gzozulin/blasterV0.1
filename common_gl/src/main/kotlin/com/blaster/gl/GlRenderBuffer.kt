package com.blaster.gl

private val backend = GlLocator.locate()

class GlRenderBuffer(
        private val component: Int = backend.GL_DEPTH_COMPONENT24,
        private val width: Int, private val height: Int) : GlBindable {

    var handle: Int? = null

    init {
        handle = glCheck { backend.glGenRenderbuffers() }
        check(handle!! > 0)
    }

    init {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle!!) }
        glCheck { backend.glRenderbufferStorage(backend.GL_RENDERBUFFER, component, width, height) }
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0) }
    }

    fun free() {
        glCheck { backend.glDeleteRenderBuffers(handle!!) }
        handle = null
    }

    override fun bind() {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle!!) }
    }

    override fun unbind() {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0) }
    }
}
