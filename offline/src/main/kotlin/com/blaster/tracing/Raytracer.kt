package com.blaster.tracing

import com.blaster.REGION_CNT
import com.blaster.WIDTH
import com.blaster.HEIGHT
import com.blaster.hitables.Hitable
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.scene.Camera
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlin.math.sqrt

private const val SAMPLES          = 50
private const val REFLECTIONS      = 20

class Raytracer(
    private val scene: Hitable,
    private val camera: Camera,
    private val ppt: PptFile,
    private val regionCnt: Int
) {
    @Volatile
    private var finished = 0

    fun render() {
        Flowable.range(0, REGION_CNT)
            .parallel()
            .runOn(Schedulers.computation())
            .doOnNext { calculateRegion(scene, it, REGION_CNT) }
            .sequential()
            .blockingLast()
        ppt.flush()
    }

    private fun calculateRegion(world: Hitable, index: Int, cnt: Int) {
        if (HEIGHT % cnt != 0) {
            throw IllegalArgumentException("Should split just fine!")
        }
        val regionLines = HEIGHT / cnt
        val regionStart = regionLines * index
        val regionEnd = regionStart + regionLines
        for (y in (regionEnd - 1) downTo regionStart) {
            for (x in 0 until WIDTH) {
                var color = Vec3()
                for (i in 0..SAMPLES) {
                    val u = (x.toFloat() + Math.random().toFloat()) / WIDTH.toFloat()
                    val v = (y.toFloat() + Math.random().toFloat()) / HEIGHT.toFloat()
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
}