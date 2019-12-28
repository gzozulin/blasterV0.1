package com.blaster.scene

import org.joml.Vector3f

data class AABB(val min: Vector3f, val max: Vector3f) {
    val width = 0f
    val height = 0f
    val depth = 0f
    val center = Vector3f()
}