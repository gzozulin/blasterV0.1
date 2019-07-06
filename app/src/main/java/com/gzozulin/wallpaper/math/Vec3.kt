package com.gzozulin.wallpaper.math

class Vec3(val values: FloatArray) {
    constructor() : this(floatArrayOf(0f, 0f, 0f))
    constructor(value: Float) : this(floatArrayOf(value, value, value))
    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) : this(floatArrayOf(x, y, z))

    var x: Float
        get() = values[0]
        set(value) {
            values[0] = value
        }

    var y: Float
        get() = values[1]
        set(value) {
            values[1] = value
        }

    var z: Float
        get() = values[2]
        set(value) {
            values[2] = value
        }

    fun to4f(w: Float) = floatArrayOf(values[0], values[1], values[2], w)

    fun randomize(max: Float): Vec3 {
        values[0] = Math.random().toFloat() * max
        values[1] = Math.random().toFloat() * max
        values[2] = Math.random().toFloat() * max
        return this
    }

    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
}