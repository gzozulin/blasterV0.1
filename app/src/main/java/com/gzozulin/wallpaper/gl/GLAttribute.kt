package com.gzozulin.wallpaper.gl

enum class GLAttribute(val label: String, val size: Int, val location: Int) {
    ATTRIBUTE_POSITION(     "aPosition",    3, 0),
    ATTRIBUTE_TEXCOORD(     "aTexCoord",    2, 1),
    ATTRIBUTE_NORMAL(       "aNormal",      3, 2),
    ATTRIBUTE_COLOR(        "aColor",       3, 3),
}