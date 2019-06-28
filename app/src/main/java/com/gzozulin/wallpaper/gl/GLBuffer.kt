package com.gzozulin.wallpaper.gl

import android.opengl.GLES30
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLBuffer(private val type: Int, buffer: Buffer, size: Int) : GLBindable {
    private val handle: Int

    // todo make two different constructors for indices/vertices
    init {
        check(type == GLES30.GL_ARRAY_BUFFER || type == GLES30.GL_ELEMENT_ARRAY_BUFFER)
    }

    init {
        val handles = IntArray(1)
        glCheck { GLES30.glGenBuffers(1, handles, 0) }
        handle = handles[0]
        check(handle > 0)
    }

    init {
        glCheck {
            GLES30.glBindBuffer(type, handle)
            GLES30.glBufferData(type, size, buffer, GLES30.GL_STATIC_DRAW)
            GLES30.glBindBuffer(type, 0)
        }
    }

    constructor(type: Int, floats: FloatArray) : this(type, ByteBuffer.allocateDirect(floats.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(floats)
            .position(0), floats.size * 4)

    constructor(type: Int, ints: IntArray) : this(type, ByteBuffer.allocateDirect(ints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(ints)
            .position(0), ints.size * 4)

    override fun bind() {
        glCheck { GLES30.glBindBuffer(type, handle) }
    }

    override fun unbind() {
        glCheck { GLES30.glBindBuffer(type, 0) }
    }
}