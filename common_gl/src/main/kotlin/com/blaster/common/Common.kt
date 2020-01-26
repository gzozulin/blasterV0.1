package com.blaster.common

import org.joml.*
import java.lang.Math
import java.util.Random
import java.util.regex.Pattern

typealias vec3 = Vector3f
typealias euler3 = Vector3f
typealias color = Vector3f
typealias vec2 = Vector2f
typealias mat4 = Matrix4f
typealias quat = Quaternionf
typealias aabb = AABBf

fun fail(message: String = "Failed!") {
    throw IllegalArgumentException(message)
}

fun fail(throwable: Throwable) {
    throw IllegalArgumentException(throwable)
}

// todo: remove - mutable
val VECTOR_UP = Vector3f(0f, 1f, 0f)
val VECTOR_DOWN = Vector3f(0f, -1f, 0f)

fun radf(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun radf(degrees: Double) = Math.toRadians(degrees).toFloat()

fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to

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

// todo: a little bit of parsing inefficiency down there:

fun String.toVec3(): vec3 {
    val tokens = this.split(Pattern.compile("\\s+"))
    return when (tokens.size) {
        3 -> vec3(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat())
        1 -> vec3(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
        else -> throw IllegalArgumentException()
    }
}

fun String.toQuat(): quat {
    val tokens = this.split(Pattern.compile("\\s+"))
    return when (tokens.size) {
        4 -> quat(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
        1 -> quat(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
        else -> throw IllegalArgumentException()
    }
}

fun String.toAabb(): aabb {
    val tokens = this.split(Pattern.compile("\\s+"))
    return when (tokens.size) {
        6 -> aabb(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat(), tokens[4].toFloat(), tokens[5].toFloat())
        1 -> aabb(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
        else -> throw IllegalArgumentException()
    }
}