package com.blaster.common

import org.joml.Vector3f

data class AABB (val min: Vector3f = Vector3f(), val max: Vector3f = Vector3f()) {
    val width: Float
        get() = max.x - min.x

    val height: Float
        get() = max.y - min.y

    val depth: Float
        get() = max.z - min.z

    val center: Vector3f
        get() = Vector3f(
                min.x + (max.x - min.x) / 2f,
                min.y + (max.y - min.y) / 2f,
                min.z + (max.z - min.z) / 2f)
}