package com.blaster.gl

import com.blaster.auxiliary.color

private val backend = GlLocator.locate()

class GlState private constructor() {
    companion object {
        fun enableDepthTest() {
            glCheck {
                backend.glEnable(backend.GL_DEPTH_TEST)
                backend.glDepthFunc(backend.GL_LEQUAL)
            }
        }

        fun disableDepthTest() {
            glCheck { backend.glDisable(backend.GL_DEPTH_TEST) }
        }

        fun drawWithNoDepth(draw: () -> Unit) {
            disableDepthTest()
            draw.invoke()
            enableDepthTest()
        }

        fun enableCulling() {
            glCheck { backend.glFrontFace(backend.GL_CCW) }
            glCheck { backend.glEnable(backend.GL_CULL_FACE) }
        }

        fun disableCulling() {
            glCheck { backend.glDisable(backend.GL_CULL_FACE) }
        }

        fun drawWithNoCulling(draw: () -> Unit) {
            disableCulling()
            draw.invoke()
            enableCulling()
        }

        fun setClearColor(color: color) {
            glCheck { backend.glClearColor(color.x, color.y, color.z, 1f) }
        }

        fun apply(width: Int, height: Int, color: color = color(0.9f, 9.9f, 1f)) {
            glCheck { backend.glViewport(0, 0, width, height) }
            glCheck { backend.glEnable(backend.GL_MULTISAMPLE) }
            setClearColor(color)
            enableDepthTest()
            enableCulling()
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