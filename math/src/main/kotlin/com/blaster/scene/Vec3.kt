package com.blaster.scene

import org.joml.Vector3f
import java.util.*
import kotlin.math.sqrt

data class Vec3(val value: Vector3f = Vector3f()) {
    val x
        get() = value.x

    val y
        get() = value.y

    val z
        get() = value.z

    constructor(x: Float = 0f, y: Float = 0f, z: Float = 0f) : this (Vector3f(x, y, z))

    constructor(f: Float) : this(f, f, f)

    constructor(other: Vec3) : this(other.x, other.y, other.z)

    operator fun get(axis: Int) = value[axis]

    fun setX(newX: Float) = Vec3(newX, y, z)
    fun setY(newY: Float) = Vec3(x, newY, z)
    fun setZ(newZ: Float) = Vec3(x, y, newZ)

    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun plus(f: Float) = Vec3(x + f, y + f, z + f)

    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)

    operator fun times(f: Float) = Vec3(x * f, y * f, z * f)
    operator fun times(other: Vec3) = Vec3(x * other.x, y * other.y, z * other.z)

    operator fun div(f: Float) = Vec3(x / f, y / f, z / f)

    fun dot(other: Vec3) = (x * other.x) + (y * other.y) + (z * other.z)
    fun cross(other: Vec3) = Vec3(
            (y * other.z) - (z * other.y),
            (z * other.x) - (x * other.z),
            (x * other.y) - (y * other.x)
    )

    fun length() = sqrt(x * x + y * y + z * z)
    fun squaredLength() = x * x + y * y + z * z

    fun makeUnit() = this / length()

    fun negate() = Vec3(-x, -y, -z)

    companion object {
        private val random = Random()

        fun randomVec3(max: Float) = Vec3(Math.random().toFloat() * max, Math.random().toFloat() * max, Math.random().toFloat() * max)

        fun randomInUnitSphere(): Vec3 {
            var result: Vec3
            do {
                result = Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat()) * 2f - Vec3(1f)
            } while (result.squaredLength() >= 1f)
            return result
        }

        fun randomInUnitDisk(): Vec3 {
            var result: Vec3
            do {
                result = Vec3(random.nextFloat(), random.nextFloat(), 0f) * 2f - Vec3(1f, 1f, 0f)
            } while (result.dot(result) >= 1f)
            return result
        }

        fun reflect(vec: Vec3, normal: Vec3): Vec3 {
            return vec - normal * vec.dot(normal) * 2f
        }

        fun refract(vec: Vec3, normal: Vec3, niOverNt: Float): Vec3? {
            val uv = vec.makeUnit()
            val dot = uv.dot(normal)
            val discriminant = 1f - niOverNt * niOverNt * (1f - dot * dot)
            if (discriminant <= 0f) {
                return null
            }
            return (uv - normal * dot) * niOverNt - normal * sqrt(discriminant)
        }
    }
}
