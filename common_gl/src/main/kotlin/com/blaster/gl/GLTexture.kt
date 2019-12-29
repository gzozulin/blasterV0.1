package com.blaster.gl

import java.nio.Buffer

private val backend = GLLocator.instance()

class GLTexture(
        val target: Int = backend.GL_TEXTURE_2D,
        val unit: Int = 0,
        private val width: Int,
        private val height: Int,
        internalFormat: Int = backend.GL_RGBA,
        pixelFormat: Int = backend.GL_RGBA,
        pixelType: Int = backend.GL_UNSIGNED_BYTE,
        pixels: Buffer? = null) : GLBindable {

    val handle: Int = glCheck { backend.glGenTextures() }

    init {
        check(handle > 0)
    }

    init {
        glCheck { backend.glBindTexture(target, handle) }
        glCheck { backend.glTexImage2D(target, 0, internalFormat, width, height, 0, pixelFormat, pixelType, pixels) }
        glCheck { backend.glTexParameteri(target, backend.GL_TEXTURE_MIN_FILTER, backend.GL_NEAREST) }
        glCheck { backend.glTexParameteri(target, backend.GL_TEXTURE_MAG_FILTER, backend.GL_NEAREST) }
        glCheck { backend.glTexParameteri(backend.GL_TEXTURE_2D, backend.GL_TEXTURE_WRAP_S, backend.GL_REPEAT) }
        glCheck { backend.glTexParameteri(backend.GL_TEXTURE_2D, backend.GL_TEXTURE_WRAP_T, backend.GL_REPEAT) }
        glCheck { backend.glBindTexture(target, 0) }
    }

    override fun bind() {
        glCheck { backend.glActiveTexture(backend.GL_TEXTURE0 + unit) } // passing GL_TEXTURE1?
        glCheck { backend.glBindTexture(target, handle) }
    }

    override fun unbind() {
        glCheck {
            backend.glActiveTexture(backend.GL_TEXTURE0 + unit)
            backend.glBindTexture(target, 0)
        }
    }
}