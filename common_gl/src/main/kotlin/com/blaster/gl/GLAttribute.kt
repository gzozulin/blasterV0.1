package com.blaster.gl

enum class GLAttribute(val size: Int, val location: Int) {
    ATTRIBUTE_POSITION( 3, 0),
    ATTRIBUTE_TEXCOORD( 2, 1),
    ATTRIBUTE_NORMAL(   3, 2),
    ATTRIBUTE_COLOR(    3, 3),
}