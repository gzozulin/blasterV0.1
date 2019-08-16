package com.blaster.math

import kotlin.math.max
import kotlin.math.min

data class AABB(val min: Vec3, val max: Vec3) {
    val center = min + (max - min) / 2f

    fun hit(ray: Ray, tMin: Float, tMax: Float): Boolean {
        var invD: Float
        var t1: Float
        var t0: Float
        for (axis in 0..2) {
            invD = 1f / ray.direction[axis]
            if (invD < 0f) {
                t0 = (max[axis] - ray.origin[axis]) * invD
                t1 = (min[axis] - ray.origin[axis]) * invD
            } else {
                t0 = (min[axis] - ray.origin[axis]) * invD
                t1 = (max[axis] - ray.origin[axis]) * invD
            }
            if (t0 < tMin) {
                t0 = tMin
            }
            if (t1 > tMax) {
                t1 = tMax
            }
            if (t1 < t0) {
                return false
            }
        }
        return true
    }

    operator fun plus(other: AABB): AABB {
        val min = Vec3(
            min(min.x, other.min.x),
            min(min.y, other.min.y),
            min(min.z, other.min.z)
        )
        val max = Vec3(
            max(max.x, other.max.x),
            max(max.y, other.max.y),
            max(max.z, other.max.z)
        )
        return AABB(min, max)
    }

    fun maxAxisIndex(): Int {
        var value = max[0] - min[0]
        var selected = 0
        for (axis in 1..2) {
            val newVal = max[axis] - min[axis]
            if (newVal > value) {
                selected = axis
                value = newVal
            }
        }
        return selected
    }
}