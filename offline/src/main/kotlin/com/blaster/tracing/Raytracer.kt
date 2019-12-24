package com.blaster.tracing

import com.blaster.Hitable
import com.blaster.scene.Ray
import com.blaster.scene.Vec3
import com.blaster.scene.Camera
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sqrt

class Raytracer(
        private val height: Int,
        private val width: Int,
        private val scene: Hitable,
        private val camera: Camera,
        private val ppt: PptFile,
        private val regionCnt: Int
) {
    @Volatile
    private var finished = 0

    fun render() = runBlocking {
        val results = ConcurrentLinkedQueue<Job>()
        (0 until regionCnt).forEach {
            results.add(launch (Dispatchers.IO) { calculateRegion(scene, it, regionCnt) })
        }
        results.joinAll()
        ppt.flush()
    }

    private fun calculateRegion(world: Hitable, index: Int, cnt: Int) {
        check(height % cnt == 0) { "Should split just fine!" }
        val regionLines = height / cnt
        val regionStart = regionLines * index
        val regionEnd = regionStart + regionLines
        for (y in (regionEnd - 1) downTo regionStart) {
            for (x in 0 until width) {
                var color = Vec3()
                for (i in 0..SAMPLES) {
                    val u = (x.toFloat() + Math.random().toFloat()) / width.toFloat()
                    val v = (y.toFloat() + Math.random().toFloat()) / height.toFloat()
                    val ray = camera.getRay(u, v)
                    color += color(ray, world, 0)
                }
                color /= SAMPLES.toFloat()
                color = Vec3(sqrt(color.x), sqrt(color.y), sqrt(color.z))
                ppt.appendRegion(index, color)
            }
        }
        regionFinished()
    }

    private fun color(ray: Ray, world: Hitable, depth: Int): Vec3 {
        val hit = world.hit(ray, 0.001f, Float.MAX_VALUE) ?: return Vec3()
        var result = hit.material.emitted(hit.u, hit.v, hit.point)
        if (depth >= REFLECTIONS) {
            return result
        }
        val scatterResult = hit.material.scattered(ray, hit)
        if (scatterResult != null) {
            result += scatterResult.attenuation * color(scatterResult.scattered, world, depth + 1)
        }
        return result
    }

    private fun regionFinished() {
        finished++
        val format = String.format("%.2f", finished.toFloat() / regionCnt.toFloat())
        println(format)
    }

    private companion object {
        private const val SAMPLES          = 50
        private const val REFLECTIONS      = 20
    }
}