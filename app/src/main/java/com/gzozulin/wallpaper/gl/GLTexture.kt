package com.gzozulin.wallpaper.gl

import android.opengl.GLES20
import java.nio.Buffer

class GLTexture(
        val target: Int = GLES20.GL_TEXTURE_2D,
        internalFormat: Int = GLES20.GL_RGBA,
        private val minFilter: Int = GLES20.GL_NEAREST,
        private val magFilter: Int = GLES20.GL_NEAREST,
        private val width: Int,
        private val height: Int,
        pixels: Buffer?,
        pixelFormat: Int = GLES20.GL_RGB,
        pixelType: Int = GLES20.GL_FLOAT) : GLBindable {

    val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES20.glGenTextures(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck { GLES20.glTexImage2D(target, 0, internalFormat, width, height, 0, pixelFormat, pixelType, pixels) }
    }

    init {
        glCheck {
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, minFilter)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, magFilter)
        }
    }

    override fun bind() {
        glCheck { GLES20.glBindTexture(target, handle) }
    }

    override fun unbind() {
        glCheck { GLES20.glBindTexture(target, 0) }
    }
}