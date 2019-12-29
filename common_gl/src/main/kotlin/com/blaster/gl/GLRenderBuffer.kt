package com.blaster.gl

private val backend = GLLocator.instance()

class GLRenderBuffer(
        private val component: Int = backend.GL_DEPTH_COMPONENT24,
        private val width: Int, private val height: Int) : GLBindable {

    val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { backend.glGenRenderbuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle) }
        glCheck { backend.glRenderbufferStorage(backend.GL_RENDERBUFFER, component, width, height) }
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0) }
    }

    override fun bind() {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, handle) }
    }

    override fun unbind() {
        glCheck { backend.glBindRenderbuffer(backend.GL_RENDERBUFFER, 0) }
    }
}
