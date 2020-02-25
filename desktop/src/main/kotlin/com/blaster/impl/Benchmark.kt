package com.blaster.impl

import com.blaster.auxiliary.randf
import com.blaster.auxiliary.vec3
import kotlin.system.measureNanoTime

private const val CNT = 5000000
private const val CNT_WARMUP = (CNT * 0.1).toInt()

data class AllocatingVec(val x: Float = 0f,
                         val y: Float = 0f,
                         val z: Float = 0f) {
    fun cross(allocating: AllocatingVec): AllocatingVec {
        return AllocatingVec(
                y * allocating.z - z * allocating.y,
                z * allocating.x - x * allocating.z,
                x * allocating.y - y * allocating.x)
    }
}

val vectors1 = mutableListOf<vec3>()
val vectors2 = mutableListOf<vec3>()
val vectors3 = mutableListOf<vec3>()
val vectorsResult = mutableListOf<vec3>()

fun generateVectors() {
    for (i in 0..CNT) {
        vectors1.add(vec3(randf(), randf(), randf()))
        vectors2.add(vec3(randf(), randf(), randf()))
        vectors3.add(vec3(randf(), randf(), randf()))
    }
}

fun warmupVectors() {
    for (i in 0..CNT_WARMUP) {
        val vector1 = vectors1[i]
        val vector2 = vectors2[i]
        val vector3 = vectors3[i]
        vectorsResult.add(vector1.cross(vector2).cross(vector3))
    }
}

fun benchmarkVectors() {
    for (i in CNT_WARMUP..CNT) {
        val vector1 = vectors1[i]
        val vector2 = vectors2[i]
        val vector3 = vectors3[i]
        vectorsResult.add(vector1.cross(vector2).cross(vector3))
    }
}

val others1 = mutableListOf<AllocatingVec>()
val others2 = mutableListOf<AllocatingVec>()
val others3 = mutableListOf<AllocatingVec>()
val othersResult = mutableListOf<AllocatingVec>()

fun generateOthers() {
    for (i in 0..CNT) {
        others1.add(AllocatingVec(randf(), randf(), randf()))
        others2.add(AllocatingVec(randf(), randf(), randf()))
        others3.add(AllocatingVec(randf(), randf(), randf()))
    }
}

fun warmupOthers() {
    for (i in 0..CNT_WARMUP) {
        val vector1 = others1[i]
        val vector2 = others2[i]
        val vector3 = others3[i]
        othersResult.add(vector1.cross(vector2).cross(vector3))
    }
}

fun benchmarkOthers() {
    for (i in CNT_WARMUP..CNT) {
        val vector1 = others1[i]
        val vector2 = others2[i]
        val vector3 = others3[i]
        othersResult.add(vector1.cross(vector2).cross(vector3))
    }
}

fun main() {
    generateVectors()
    warmupVectors()
    val measured = measureNanoTime { benchmarkVectors() }
    println("Vectors: $measured nanos")
    generateOthers()
    warmupOthers()
    val others = measureNanoTime { benchmarkOthers() }
    println("Allocating: $others nanos")
}