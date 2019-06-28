package com.gzozulin.wallpaper.gl

import android.opengl.GLES30
import java.nio.Buffer

class GLTexture(
        private val active: Int = GLES30.GL_TEXTURE0,
        val target: Int = GLES30.GL_TEXTURE_2D,
        internalFormat: Int = GLES30.GL_RGBA,
        private val minFilter: Int = GLES30.GL_NEAREST,
        private val magFilter: Int = GLES30.GL_NEAREST,
        private val width: Int,
        private val height: Int,
        pixels: Buffer? = null,
        pixelFormat: Int = GLES30.GL_RGB,
        pixelType: Int = GLES30.GL_FLOAT) : GLBindable {

    val handle: Int

    init {
        val handles = IntArray(1)
        glCheck { GLES30.glGenTextures(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck {
            GLES30.glBindTexture(target, handle)
            GLES30.glTexImage2D(target, 0, internalFormat, width, height, 0, pixelFormat, pixelType, pixels)
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, minFilter)
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, magFilter)
            GLES30.glBindTexture(target, 0)
        }
    }

    override fun bind() {
        glCheck {
            GLES30.glActiveTexture(active)
            GLES30.glBindTexture(target, handle)
        }
    }

    override fun unbind() {
        glCheck {
            GLES30.glActiveTexture(active)
            GLES30.glBindTexture(target, 0)
        }
    }
}