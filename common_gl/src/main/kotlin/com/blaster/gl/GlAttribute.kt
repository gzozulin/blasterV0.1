package com.blaster.gl

private val backend = GlLocator.locate()

enum class GlAttribute(val size: Int, val location: Int) {
    ATTRIBUTE_POSITION( 3, 0),
    ATTRIBUTE_TEXCOORD( 2, 1),
    ATTRIBUTE_NORMAL(   3, 2),
    ATTRIBUTE_COLOR(    3, 3),
    ATTRIBUTE_IS_ALIVE( 1, 4),
    ATTRIBUTE_OFFSET(   3, 5);

    companion object {
        fun bindVertexPointers(attributes: List<GlAttribute>) {
            var stride = 0
            var offset = 0L
            attributes.forEach { stride += it.size * 4 }
            attributes.forEach {
                glCheck {
                    backend.glEnableVertexAttribArray(it.location)
                    backend.glVertexAttribPointer(it.location, it.size, backend.GL_FLOAT, false, stride, offset)
                }
                offset += it.size * 4
            }
        }

        fun disableVertexPointers(attributes: List<GlAttribute>) {
            attributes.forEach {
                glCheck { backend.glDisableVertexAttribArray(it.location) }
            }
        }
    }
}