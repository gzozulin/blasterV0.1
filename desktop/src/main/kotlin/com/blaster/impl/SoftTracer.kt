package com.blaster.impl

import com.blaster.auxiliary.back
import com.blaster.auxiliary.ray
import com.blaster.auxiliary.vec3
import com.blaster.platform.LwjglWindow

private const val VIEWPORT_WIDTH = 300
private const val VIEWPORT_HEIGHT = 200

private const val VIEWPORT_L = -VIEWPORT_WIDTH / 2f
private const val VIEWPORT_R = VIEWPORT_WIDTH / 2f
private const val VIEWPORT_B = -VIEWPORT_HEIGHT / 2f
private const val VIEWPORT_T = VIEWPORT_HEIGHT / 2f

private const val FOCUS_DISTANCE = 1.0f

private class RtrCamera {
    private val position = vec3().zero()
    private val direction = vec3().back()

    fun ray(i: Int, j: Int): ray {
        val u = VIEWPORT_L + (VIEWPORT_R - VIEWPORT_L) * (i + 0.5f) / VIEWPORT_WIDTH
        val v = VIEWPORT_B + (VIEWPORT_T - VIEWPORT_B) * (i + 0.5f) / VIEWPORT_HEIGHT

        val rayDir = vec3()
        direction.negate(rayDir)
        rayDir.mul(FOCUS_DISTANCE)

        // ????


        return ray(position, rayDir)
    }
}

private interface Hitable {
    // checks for intersection
}

private val camera = RtrCamera()

private class SoftTracer {
    fun render() {
        for (i in 0 until VIEWPORT_WIDTH) {
            for (j in 0 until VIEWPORT_HEIGHT) {
                val ray = camera.ray(i, j)
            }
        }
    }
}

private val window = object : LwjglWindow() {
    override fun onCreate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResize(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTick() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}