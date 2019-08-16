package com.blaster

import com.blaster.math.Vec3
import com.blaster.scene.Camera
import com.blaster.tracing.Raytracer
import com.blaster.tracing.PptFile
import com.blaster.scene.CornellScene
import kotlin.system.measureNanoTime

fun main() {
    // header h1 Creating an offline raytracer.

    /* In the modern day and age, there is probably no human being, who have an access to the internet and did not saw any computer
    generated graphics. Since you obviously are online, I am assuming, that you are not the one :)
    Computer graphics is a widely adopted way to generate images on the screen, or even sequences of images. If you will add
    an interactivity to the mix - you will have a full blown video game. */

    /* In this demo, we will have a look at a subset of all possible graphics generators - the offline raytracer. There are two main
    approaches: online and offline. The difference is substantial: offline renderers can produce much better imagery at the expense of the
    speed. Such software can be used to produce film-quality material, but video games are out of the reach. Non the less - it is a very
    interesting project to accomplish and it can showcase the most important concept of Computer Graphics - how the light interacts with
    the surface and the resulting image is produced. */

    // Here is a sneak peak on our expected results:
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