package com.blaster.gl

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GLLocator.instance()

class GLBuffer(private val type: Int, buffer: Buffer, size: Int) : GLBindable {
    private val handle: Int = glCheck { backend.glGenBuffers() }

    // todo make two different constructors for indices/vertices
    init {
        check(type == backend.GL_ARRAY_BUFFER || type == backend.GL_ELEMENT_ARRAY_BUFFER)
        check(handle > 0)
    }

    init {
        glCheck {
            backend.glBindBuffer(type, handle)
            backend.glBufferData(type, size, buffer, backend.GL_STATIC_DRAW)
            backend.glBindBuffer(type, 0)
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
        glCheck { backend.glBindBuffer(type, handle) }
    }

    override fun unbind() {
        glCheck { backend.glBindBuffer(type, 0) }
    }
}