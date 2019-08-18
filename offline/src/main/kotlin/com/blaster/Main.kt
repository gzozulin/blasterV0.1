package com.blaster

import com.blaster.math.Vec3
import com.blaster.scene.Camera
import com.blaster.tracing.Raytracer
import com.blaster.tracing.PptFile
import com.blaster.scene.CornellScene
import kotlin.system.measureNanoTime

fun main() {
    // header h1 Creating a software raytracer in Kotlin.

    // header h2 A picture is worth a thousand words.

    /* Hello and welcome to our demo! */

    /* Computer Graphics is ever-present nowadays. You can find it in movies, video games,
    interactive presentations - almost everywhere. But what actually happens behind the scenes? In
    this demo, we will go step by step through the process of the creation of an image from the
    scene description. */

    /* Let's start with basics: there is a multitude of formats in which an image can be represented
    on a computer system. Most commonly known are jpeg, png, bmp, gif formats. Most of those image
    formats include some kind of compression or animation or both. For our demo, we will choose a
    slightly less common format - a ppt file. What is good about it - is the fact that it is
    human-readable. We also can modify it easily, without elaborate compression routines. The code
    for it will be shown later on. */

    /* Next step from a standalone image is an animation. The difference is very simple - while an
    image represents a static scene, the animation is a sequence of images - snapshots of the same
    scene, happened during a timeframe. We can even make those animations interactive - by adding
    user input. And voila - we have a video game. */

    /* In this demo, we will be working on a software raytracer. This means that our raytracer will
    not be using specialized hardware - which is known as Graphics Processing Unit (or GPU in
    short). */

    // Here are some of the results produced by our end-product:
    // include picture Raytraced image; This image was produced with the raytracer explained in this article;https://camo.githubusercontent.com/e0c2987577fb4a962731d007e5151ab351862144/68747470733a2f2f692e696d6775722e636f6d2f6b464d546375382e6a7067

    // header h2 The Raytracer class.

    /* I will start my explanation of the code with the main method. The only purpose of it is to call is to create the necessary objects
     and call for the execution. We start by declaring a set of constants, which will define the parameters of our raytracer. Those are
     self explanatory: */
    val width            = 500
    val height           = 500

    /* Regions count is a bit more tricky. Usually, graphics applications are a highly parallelised software. It means, that to speedup
    processing, we want to employ as many executors as possible. In our case, since we are doing the task in software - those executors
    are our cores. Since we want to fill them as tightly as possible, we will split the image into independent parts - regions - and
    compute them independently. */
    val regionsCnt       = 100

    /* The next thing we will create is our ppt file abstraction. */
    val ppt = PptFile(width, height, regionsCnt)

    // include decl com.blaster.tracing.PptFile

    /* Our scene will be represented by the famous Cornell Box. */
    val scene = CornellScene()

    // include def com.blaster.scene.CornellScene::prepare

    /* We will setup our camera to look into the center of the scene from far away by Z: */
    val eye = scene.aabb().center.setZ(-800f)
    val center = scene.aabb().center
    val up = Vec3(y = 1f)

    /* Camera requires a couple of additional parameters like aspect ratio, aperture, field of view and etc. */
    val camera = Camera(
            eye, center, up,
            40f, width.toFloat() / height.toFloat(), 0f, 10f
    )

    /* Finally, we will create a Raytracer instance. */
    val raytracer = Raytracer(width, height, scene, camera, ppt, regionsCnt)

    /* We also want to measure a time, which is needed for the operation. Later we can use this time for profiling purposes. */
    val nanoTime = measureNanoTime { raytracer.render() }
    val seconds = "%.2f".format(nanoTime.toFloat() / 1000000000f)
    print("Operation took $seconds seconds")

    // inline def com.blaster.tracing.Raytracer::render
}