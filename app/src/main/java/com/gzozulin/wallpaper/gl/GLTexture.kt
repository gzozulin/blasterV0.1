package com.gzozulin.wallpaper.gl

import android.opengl.GLES30
import java.nio.Buffer

class GLTexture(
        val target: Int = GLES30.GL_TEXTURE_2D,
        val unit: Int = 0,
        private val width: Int,
        private val height: Int,
        internalFormat: Int = GLES30.GL_RGBA,
        pixelFormat: Int = GLES30.GL_RGBA,
        pixelType: Int = GLES30.GL_FLOAT,
        pixels: Buffer? = null) : GLBindable {

    val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES30.glGenTextures(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck { GLES30.glBindTexture(target, handle) }
        glCheck { GLES30.glTexImage2D(target, 0, internalFormat, width, height, 0, pixelFormat, pixelType, pixels) }
        glCheck { GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST) }
        glCheck { GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST) }
        glCheck { GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT) }
        glCheck { GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT) }
        glCheck { GLES30.glBindTexture(target, 0) }
    }

    override fun bind() {
        glCheck {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
            GLES30.glBindTexture(target, handle)
        }
    }

    override fun unbind() {
        glCheck {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
            GLES30.glBindTexture(target, 0)
        }
    }
}