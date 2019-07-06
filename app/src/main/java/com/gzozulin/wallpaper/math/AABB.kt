package com.gzozulin.wallpaper.math

data class AABB (val min: Vec3, val max: Vec3) {
    constructor() : this(Vec3(Float.MAX_VALUE), Vec3(-Float.MAX_VALUE))

    val width: Float
        get() = max.x - min.x

    val height: Float
        get() = max.y - min.y

    val depth: Float
        get() = max.z - min.z

    val center: Vec3
        get() = Vec3(
                min.x + (max.x - min.x) / 2f,
                min.y + (max.y - min.y) / 2f,
                min.z + (max.z - min.z) / 2f)

    fun include(vx: Float, vy: Float, vz: Float) {
        if (vx < min.x) {
            min.x = vx
        }
        if (vx > max.x) {
            max.x = vx
        }
        if (vy < min.y) {
            min.y = vy
        }
        if (vy > max.y) {
            max.y = vy
        }
        if (vz < min.z) {
            min.z = vz
        }
        if (vz > max.z) {
            max.z = vz
        }
    }
}