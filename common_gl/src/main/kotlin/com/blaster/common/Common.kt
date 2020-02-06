package com.blaster.common

import org.joml.*
import java.lang.Math
import java.util.Random
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

typealias vec3 = Vector3f
typealias euler3 = Vector3f
typealias color = Vector3f
typealias vec2 = Vector2f
typealias mat3 = Matrix3f
typealias mat4 = Matrix4f
typealias quat = Quaternionf
typealias aabb = AABBf

private val random = Random()

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
fun degf(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()
fun degf(radians: Double) = Math.toDegrees(radians).toFloat()

fun sinf(value: Float) = sin(value.toDouble()).toFloat()
fun sinf(value: Double) = sin(value).toFloat()

fun cosf(value: Float) = cos(value.toDouble()).toFloat()
fun cosf(value: Double) = cos(value).toFloat()

fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to

fun randomf(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE) =
        min + random.nextFloat() * (max - min)

fun randomi(min: Int = Integer.MIN_VALUE, max: Int = Integer.MAX_VALUE) = random.nextInt((max - min) + 1) + min
fun randomb() = random.nextBoolean()

fun extractColors(hex: String): Vector3f {
    val integerHex = Integer.parseInt(hex, 16)
    val rIntValue = (integerHex / 256 / 256) % 256
    val gIntValue = (integerHex / 256      ) % 256
    val bIntValue = (integerHex            ) % 256
    return Vector3f(rIntValue / 255.0f, gIntValue / 255.0f, bIntValue / 255.0f)
}

fun vec3.random(min: vec3 = vec3(0f), max: vec3 = vec3(1f)): vec3 {
    x = randomf(min.x, max.x)
    y = randomf(min.y, max.y)
    z = randomf(min.z, max.z)
    return this
}

// region -------------------------- AABB --------------------------

fun aabb.width() = maxX - minX
fun aabb.height() = maxY - minY
fun aabb.depth() = maxZ - minZ
fun aabb.center() = Vector3f(minX + (maxX - minX) / 2f, minY + (maxY - minY) / 2f, minZ + (maxZ - minZ) / 2f)

fun aabb.scaleTo(to: Float): Float {
    var maxSide = Float.MIN_VALUE
    if (width() > maxSide) {
        maxSide = width()
    }
    if (height() > maxSide) {
        maxSide = height()
    }
    if (depth() > maxSide) {
        maxSide = depth()
    }
    return to / maxSide
}

fun aabb.scaleTo(other: aabb) =
        vec3(other.width() / width(), other.height() / height(), other.depth() / depth())

// endregion -------------------------- AABB --------------------------

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