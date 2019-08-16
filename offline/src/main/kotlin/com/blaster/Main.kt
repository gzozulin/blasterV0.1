package com.blaster

import com.blaster.math.Vec3
import com.blaster.scene.Camera
import com.blaster.tracing.Raytracer
import com.blaster.tracing.PptFile
import com.blaster.scene.CornellScene
import kotlin.system.measureNanoTime

const val WIDTH            = 500
const val HEIGHT           = 500
const val REGION_CNT       = 100

private val ppt = PptFile(WIDTH, HEIGHT, REGION_CNT)

private val scene = CornellScene()

val eye = scene.aabb().center.setZ(-800f)
val center = scene.aabb().center
val up = Vec3(y = 1f)

val camera = Camera(
    eye, center, up,
    40f, WIDTH.toFloat() / HEIGHT.toFloat(), 0f, 10f
)

val raytracer = Raytracer(scene, camera, ppt, REGION_CNT)

fun main() {
    val nanoTime = measureNanoTime { raytracer.render() }
    val seconds = "%.2f".format(nanoTime.toDouble() / 1000000000.0)
    print("Operation took $seconds seconds")
}