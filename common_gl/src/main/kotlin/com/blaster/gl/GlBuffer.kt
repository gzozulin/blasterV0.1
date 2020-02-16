package com.blaster.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

// todo: make two different constructors for indices/vertices
class GlBuffer(
        private val target: Int,
        private val buffer: ByteBuffer,
        private val usage: Int = backend.GL_STATIC_DRAW) : GlBindable {

    private val handle: Int = glCheck { backend.glGenBuffers() }

    init {
        check(target == backend.GL_ARRAY_BUFFER || target == backend.GL_ELEMENT_ARRAY_BUFFER)
        check(handle > 0)
        uploadBuffer()
    }

    private fun uploadBuffer() {
        glCheck {
            backend.glBindBuffer(target, handle)
            backend.glBufferData(target, buffer, usage)
            backend.glBindBuffer(target, 0)
        }
    }

    override fun bind() {
        glCheck { backend.glBindBuffer(target, handle) }
    }

    override fun unbind() {
        glCheck { backend.glBindBuffer(target, 0) }
    }

    private fun mapBuffer(access: Int): ByteBuffer = glCheck {  backend.glMapBuffer(target, access, buffer) }
    private fun unmapBuffer() = glCheck { backend.glUnapBuffer(target) }

    fun updateBuffer(access : Int = backend.GL_WRITE_ONLY, update: (mapped: ByteBuffer) -> Unit) {
        val mapped = mapBuffer(access)
        update.invoke(mapped)
        unmapBuffer()
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
}