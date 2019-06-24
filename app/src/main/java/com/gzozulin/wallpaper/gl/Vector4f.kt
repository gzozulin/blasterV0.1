package com.gzozulin.wallpaper.gl

data class Vector4f(val values: FloatArray) {
    constructor() : this(floatArrayOf(0f, 0f, 0f, 0f))
    constructor(x: Float, y: Float, z: Float) : this(floatArrayOf(x, y, z, 1f))
    constructor(x: Float, y: Float, z: Float, w: Float) : this(floatArrayOf(x, y, z, w))
}