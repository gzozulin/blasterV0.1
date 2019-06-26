package com.gzozulin.wallpaper.gl

data class Vector3f(val values: FloatArray) {
    constructor() : this(floatArrayOf(0f, 0f, 0f))
    constructor(x: Float, y: Float, z: Float) : this(floatArrayOf(x, y, z))

    fun to4f(w: Float) = floatArrayOf(values[0], values[1], values[2], w)
}