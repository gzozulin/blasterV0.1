package com.blaster

import com.blaster.math.Vec3
import com.blaster.toolbox.Camera
import com.blaster.tracing.Raytracer
import com.blaster.tracing.PptFile
import com.blaster.toolbox.CornellScene
import kotlin.system.measureNanoTime

const val width            = 500
const val height           = 500
const val regionsCnt       = 100

val ppt = PptFile(width, height, regionsCnt)

val scene = CornellScene()

val eye = scene.aabb().center.copy(z =-800f)
val center = scene.aabb().center
val up = Vec3(y = 1f)
val camera = Camera(eye, center, up, 40f, width.toFloat() / height.toFloat(), 0f, 10f)

val raytracer = Raytracer(width, height, scene, camera, ppt, regionsCnt)

fun main() {
    val nanoTime = measureNanoTime { raytracer.render() }
    val seconds = "%.2f".format(nanoTime.toFloat() / 1000000000f)
    print("Operation took $seconds seconds")
}