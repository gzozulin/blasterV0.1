package com.blaster.gl

import org.joml.Vector3f

private val backend = GlLocator.locate()

class GlState private constructor() {
    companion object {
        fun apply(culling: Boolean = true, color: Vector3f = Vector3f(0.9f, 9.9f, 1f)) {
            glCheck { backend.glClearColor(color.x, color.y, color.z, 0f) }
            glCheck { backend.glEnable(backend.GL_DEPTH_TEST) }
            if (culling) {
                glCheck { backend.glFrontFace(backend.GL_CCW) }
                glCheck { backend.glEnable(backend.GL_CULL_FACE) }
            }
        }

        fun clear() {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
        }

        fun drawTransparent(draw: () -> Unit) {
            enableTransparency()
            draw.invoke()
            disableTransparency()
        }

        fun enableTransparency() {
            backend.glEnable(backend.GL_BLEND)
            backend.glBlendFunc(backend.GL_SRC_ALPHA, backend.GL_ONE_MINUS_SRC_ALPHA)
        }

        fun disableTransparency() {
            backend.glDisable(backend.GL_BLEND)
        }
    }
}