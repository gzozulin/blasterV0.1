package com.blaster.common

import org.joml.*
import java.lang.Math
import java.util.Random

typealias vec3 = Vector3f
typealias euler3 = Vector3f
typealias color = Vector3f
typealias vec2 = Vector2f
typealias mat4 = Matrix4f
typealias quat = Quaternionf
typealias aabb = AABBf

// todo: remove - mutable
val VECTOR_UP = Vector3f(0f, 1f, 0f)

fun radf(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun radf(degrees: Double) = Math.toRadians(degrees).toFloat()

private val random = Random()
fun randomFloat(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE) =
        min + random.nextFloat() * (max - min)

fun extractColors(hex: String): Vector3f {
    val integerHex = Integer.parseInt(hex, 16)
    val rIntValue = (integerHex / 256 / 256) % 256
    val gIntValue = (integerHex / 256      ) % 256
    val bIntValue = (integerHex            ) % 256
    return Vector3f(rIntValue / 255.0f, gIntValue / 255.0f, bIntValue / 255.0f)
}

fun vec3.random(min: vec3 = vec3(0f), max: vec3 = vec3(1f)): vec3 {
    x = randomFloat(min.x, max.x)
    y = randomFloat(min.y, max.y)
    z = randomFloat(min.z, max.z)
    return this
}

fun aabb.width() = maxX - minX
fun aabb.height() = maxY - minY
fun aabb.depth() = maxZ - minZ
fun aabb.center() = Vector3f(minX + (maxX - minX) / 2f, minY + (maxY - minY) / 2f, minZ + (maxZ - minZ) / 2f)

fun aabb.scaleTo(to: Float): Float {
    var factor = Float.MIN_VALUE
    val wF = to / width()
    if (wF > factor) {
        factor = wF
    }
    val wH = to / height()
    if (wH > factor) {
        factor = wH
    }
    val wD = to / depth()
    if (wD > factor) {
        factor = wD
    }
    return factor
}

fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to