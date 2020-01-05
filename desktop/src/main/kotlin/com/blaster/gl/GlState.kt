package com.blaster.gl

private val backend = GlLocator.locate()

class GlState {
    fun apply() {
        glCheck { backend.glClearColor(0.9f, 0.9f, 1f, 0f) }
        glCheck { backend.glEnable(backend.GL_DEPTH_TEST) }
        glCheck { backend.glFrontFace(backend.GL_CCW) }
        glCheck { backend.glEnable(backend.GL_CULL_FACE) }
    }
}