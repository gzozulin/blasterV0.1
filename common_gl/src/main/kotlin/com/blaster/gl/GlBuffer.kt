package com.blaster.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

class GlBuffer(
        private val type: Int,
        private val buffer: ByteBuffer,
        private val usage: Int = backend.GL_STATIC_DRAW) : GLBindable {

    private val handle: Int = glCheck { backend.glGenBuffers() }

    // todo make two different constructors for indices/vertices
    init {
        check(type == backend.GL_ARRAY_BUFFER || type == backend.GL_ELEMENT_ARRAY_BUFFER)
        check(handle > 0)
    }

    init {
        glCheck {
            backend.glBindBuffer(type, handle)
            backend.glBufferData(type, buffer, usage)
            backend.glBindBuffer(type, 0)
        }
    }

    companion object {
        fun create(type: Int, floats: FloatArray): GlBuffer {
            val buffer = ByteBuffer.allocateDirect(floats.size * 4).order(ByteOrder.nativeOrder())
            buffer.asFloatBuffer().put(floats).position(0)
            return GlBuffer(type, buffer)
        }

        fun create(type: Int, ints: IntArray): GlBuffer {
            val buffer = ByteBuffer.allocateDirect(ints.size * 4).order(ByteOrder.nativeOrder())
            buffer.asIntBuffer().put(ints).position(0)
            return GlBuffer(type, buffer)
        }
    }

    override fun bind() {
        glCheck { backend.glBindBuffer(type, handle) }
    }

    override fun unbind() {
        glCheck { backend.glBindBuffer(type, 0) }
    }

    fun mapBuffer(access : Int = backend.GL_MAP_WRITE_BIT or backend.GL_MAP_UNSYNCHRONIZED_BIT): ByteBuffer =
            backend.glMapBufferRange(type, 0, buffer.capacity().toLong(), access, buffer)
}