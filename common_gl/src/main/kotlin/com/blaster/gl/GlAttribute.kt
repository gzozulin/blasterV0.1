package com.blaster.gl

private val backend = GlLocator.locate()

enum class GlAttribute(val size: Int, val location: Int) {
    ATTRIBUTE_POSITION( 3, 0),
    ATTRIBUTE_TEXCOORD( 2, 1),
    ATTRIBUTE_NORMAL(   3, 2),
    ATTRIBUTE_COLOR(    3, 3),
    ATTRIBUTE_IS_ALIVE( 1, 4),
    ATTRIBUTE_OFFSET(   3, 5);
}