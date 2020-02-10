package com.blaster.aux

import org.joml.*
import java.lang.Math
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import java.util.regex.Pattern
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

fun fail(th: Throwable): Nothing = throw th
fun fail(reason: String = "wtf?!"): Nothing = fail(IllegalStateException(reason))

fun radf(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun degf(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()
fun sinf(value: Float) = sin(value.toDouble()).toFloat()
fun cosf(value: Float) = cos(value.toDouble()).toFloat()
fun lerpf(from: Float, to: Float, t: Float) = (1f - t) * from + t * to

fun randf(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE) = min + random.nextFloat() * (max - min)
fun randi(min: Int = Integer.MIN_VALUE, max: Int = Integer.MAX_VALUE) = random.nextInt((max - min) + 1) + min
fun randb() = random.nextBoolean()

fun vec3.rand(min: vec3 = vec3(0f), max: vec3 = vec3(1f)): vec3 {
    x = randf(min.x, max.x)
    y = randf(min.y, max.y)
    z = randf(min.z, max.z)
    return this
}

fun vec3.up(): vec3 {
    x = 0f
    y = 1f
    z = 0f
    return this
}

fun vec3.down(): vec3 {
    x = 0f
    y = -1f
    z = 0f
    return this
}

fun vec3.left(): vec3 {
    x = -1f
    y = 0f
    z = 0f
    return this
}

fun vec3.right(): vec3 {
    x = 1f
    y = 0f
    z = 0f
    return this
}

fun color.red(): color {
    x = 1f
    y = 0f
    z = 0f
    return this
}

fun color.green(): color {
    x = 0f
    y = 1f
    z = 0f
    return this
}

fun color.blue(): color {
    x = 0f
    y = 0f
    z = 1f
    return this
}

fun color.white(): color {
    x = 1f
    y = 1f
    z = 1f
    return this
}

fun parseColor(hex: String): color {
    val integerHex = Integer.parseInt(hex, 16)
    val rIntValue = (integerHex / 256 / 256) % 256
    val gIntValue = (integerHex / 256      ) % 256
    val bIntValue = (integerHex            ) % 256
    return color(rIntValue / 255.0f, gIntValue / 255.0f, bIntValue / 255.0f)
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

fun aabb.randomSplit(axises: List<Int> = listOf(0, 1, 2), min: Float): List<aabb> {
    val axisesCopy = ArrayList(axises)
    while(axisesCopy.isNotEmpty()) {
        val axis = axisesCopy.random()
        val length = when (axis) {
            0 -> width()
            1 -> height()
            2 -> depth()
            else -> throw IllegalStateException("wtf?!")
        }
        val from  = 0.3f
        val to = 0.7f
        val minLength = length * 0.3f
        if (minLength > min) {
            val first = randf(from, to)
            val second = 1f - first
            return splitByAxis(axis, listOf(first, second))
                    .flatMap { it.randomSplit(axises, min) }
        } else {
            axisesCopy.remove(axis)
        }
    }
    return listOf(this) // terminal
}

fun aabb.splitByAxis(axis: Int, ratios: List<Float>): List<aabb> {
    val result = mutableListOf<aabb>()
    val (from, to) = when (axis) {
        0 -> minX to maxX
        1 -> minY to maxY
        2 -> minZ to maxZ
        else -> throw IllegalArgumentException("wtf?!")
    }
    check(to > from)
    val length = to - from
    var start = from
    ratios.forEach { ratio ->
        val end = start + length * ratio
        result.add(when (axis) {
            0 -> aabb(start, minY, minZ, end, maxY, maxZ)
            1 -> aabb(minX, start, minZ, maxX, end, maxZ)
            2 -> aabb(minX, minY, start, maxX, maxY, end)
            else -> throw IllegalArgumentException("wtf?!")
        })
        start = end
    }
    return result
}

fun aabb.selectCentersInside(cnt: Int, minR: Float, maxR: Float): List<aabb> {
    check(cnt > 0 && maxR > minR)
    val result = mutableListOf<aabb>()
    while (result.size != cnt) {
        val r = randf(minR, maxR)
        val fromX = minX + r
        val toX = maxX - r
        val fromZ = minZ + r
        val toZ = maxZ - r
        val x = randf(fromX, toX)
        val z = randf(fromZ, toZ)
        result.add(aabb(x - r, minY, z - r, x + r, maxY, z + r))
    }
    return result
}

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

fun toByteBufferFloat(list: List<Float>): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asFloatBuffer()
    list.forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}

fun toByteBufferInt(list: List<Int>): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asIntBuffer()
    list.forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}