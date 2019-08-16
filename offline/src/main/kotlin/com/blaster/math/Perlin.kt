package com.blaster.math

import java.util.*

private val random = Random()

// todo: This has some problems
class Perlin {
    private val ranfloat: FloatArray = perlinGenerate()
    private val permX = generatePerm()
    private val permY = generatePerm()
    private val permZ = generatePerm()

    private fun perlinGenerate(): FloatArray {
        val result = FloatArray(256)
        for (i in 0..255) {
            result[i] = random.nextFloat()
        }
        return result
    }

    private fun generatePerm(): IntArray {
        val result = IntArray(256)
        for (i in 0..255) {
            result[i] = i
        }
        permute(result, 256)
        return result
    }

    private fun permute(array: IntArray, n: Int) {
        for (i in (n - 1) downTo 1) {
            val target = (random.nextFloat() * (i + 1)).toInt()
            val temp = array[i]
            array[i] = array[target]
            array[target] = temp
        }
    }

    fun noise(point: Vec3) : Float {
        /*val u = point.x - floorf(point.x)
        val v = point.y - floorf(point.y)
        val w = point.z - floorf(point.z)*/
        val i = ((4f * point.x).toInt() and 255)
        val j = ((4f * point.y).toInt() and 255)
        val k = ((4f * point.z).toInt() and 255)
        return ranfloat[permX[i] xor permY[j] xor permZ[k]]
    }
}