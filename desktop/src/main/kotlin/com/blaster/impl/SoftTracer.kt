package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.ShadersLib
import com.blaster.auxiliary.*
import com.blaster.entity.Controller
import com.blaster.gl.GlLocator
import com.blaster.gl.GlMesh
import com.blaster.gl.GlState
import com.blaster.gl.GlTexture
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.techniques.SimpleTechnique
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joml.Intersectionf
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

private val backend = GlLocator.locate()

private const val VIEWPORT_WIDTH = 10.2f
private const val VIEWPORT_HEIGHT = 7.6f

private const val FOCUS_DISTANCE = 10.0f

private const val VIEWPORT_LEFT = (-VIEWPORT_WIDTH / 2f).toInt()
private const val VIEWPORT_BOTTOM = (-VIEWPORT_HEIGHT / 2f).toInt()

private const val RESOLUTION_WIDTH = 1024
private const val RESOLUTION_HEIGHT = 768

private const val REGION_WIDTH = 32
private const val REGION_HEIGHT = 32

private const val REGIONS_CNT_U = RESOLUTION_WIDTH / REGION_WIDTH
private const val REGIONS_CNT_V = RESOLUTION_HEIGHT / REGION_HEIGHT

private val NANOS_PER_FRAME = TimeUnit.MILLISECONDS.toNanos(16)

private const val PIXEL_SIZE = 3 // r, g, b

private lateinit var viewportRect: GlMesh
private lateinit var viewportTexture: GlTexture

private val upVec = vec3().up()
private val viewM = mat4().identity()
private val projectionM = mat4().identity()
private val modelM = mat4().identity()

private var mouseControl = false

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)

private val camera = RtrCamera()
private val sceneVersion = Version()

private val controller = Controller(velocity = 0.1f, position = vec3().back())
private val wasdInput = WasdInput(controller)

private val simpleTechnique = SimpleTechnique()

private val scene = HitableSphere(vec3(0f, 0f, -12f), 2f, RtrMaterial())

private val regionsLow = mutableListOf<RegionTask>()
private val regionsMed = mutableListOf<RegionTask>()
private val regionsHgh = mutableListOf<RegionTask>()

private val regionsDone = mutableListOf<RegionTask>()
private val regionMutex = Mutex()

private var currentJob: Job = Job()

private val background = mutableListOf<color>()

private fun createBackground() {
    for (v in 0 until RESOLUTION_HEIGHT) {
        val color = color().cian().mul(v.toFloat() / RESOLUTION_HEIGHT)
        for (u in 0 until RESOLUTION_WIDTH) {
            background.add(color)
        }
    }
}

private class RegionTask(val u: Int, val v: Int, val uStep: Int, val vStep: Int) : Comparable<RegionTask> {
    val byteBuffer: ByteBuffer = ByteBuffer
            .allocateDirect(REGION_WIDTH * REGION_HEIGHT * PIXEL_SIZE * 4)
            .order(ByteOrder.nativeOrder())
    val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()

    val uFrom = u * REGION_WIDTH
    val vFrom = v * REGION_HEIGHT

    override fun compareTo(other: RegionTask): Int {
        val center = vec2(REGIONS_CNT_U / 2f, REGIONS_CNT_V / 2f)
        val thisSqDist = vec2(u.toFloat(), v.toFloat()).distanceSquared(center).toInt()
        val otherSqDist = vec2(other.u.toFloat(), other.v.toFloat()).distanceSquared(center).toInt()
        return thisSqDist - otherSqDist
    }
}

private fun createRegionTasks() {
    for (u in 0 until REGIONS_CNT_U) {
        for (v in 0 until REGIONS_CNT_V) {
            regionsLow.add(RegionTask(u, v, 32, 32))
        }
    }
    regionsLow.sort()
    for (u in 0 until REGIONS_CNT_U) {
        for (v in 0 until REGIONS_CNT_V) {
            regionsMed.add(RegionTask(u, v, 8, 8))
        }
    }
    regionsMed.sort()
    for (u in 0 until REGIONS_CNT_U) {
        for (v in 0 until REGIONS_CNT_V) {
            regionsHgh.add(RegionTask(u, v, 1, 1))
        }
    }
    regionsHgh.sort()
}

private class RtrCamera {
    val position: vec3 = vec3().zero()
    val direction: vec3 = vec3().back()

    private val basisX = vec3()
    private val basisY = vec3()
    private val basisZ = vec3()

    fun updateBasis() {
        direction.negate(basisZ)
        upVec.cross(basisZ, basisX)
        basisX.normalize()
        basisZ.cross(basisX, basisY)
        basisY.normalize()
    }

    fun ray(u: Float, v: Float): ray {
        val x = VIEWPORT_LEFT + VIEWPORT_WIDTH * (u + 0.5f) / RESOLUTION_WIDTH
        val y = VIEWPORT_BOTTOM + VIEWPORT_HEIGHT * (v + 0.5f) / RESOLUTION_HEIGHT
        val dir = vec3(direction).mul(FOCUS_DISTANCE)
        val uComp = vec3(basisX).mul(x)
        val vComp = vec3(basisY).mul(y)
        dir.add(uComp).add(vComp)
        dir.normalize()
        return ray(position, dir)
    }
}

private class RtrMaterial

private class HitResult(t: Float, point: vec3, normal: vec3, material: RtrMaterial)

private interface Hitable {
    fun hit(ray: ray, t0: Float, t1: Float): HitResult?
}

private class HitableSphere(private val center: vec3, private val radius: Float, private val material: RtrMaterial) : Hitable {
    private val sphere = sphere(center, radius)

    override fun hit(ray: ray, t0: Float, t1: Float): HitResult? {
        val result = vec2()
        if (Intersectionf.intersectRaySphere(ray, sphere, result)) {
            if (result.x in t0..t1) {
                return hitResult(result.x, ray)
            }
            if (result.y in t0..t1) {
                return hitResult(result.y, ray)
            }
        }
        return null
    }

    private fun hitResult(t: Float, ray: ray): HitResult {
        val point = vec3().pointAt(t, ray)
        val normal = vec3(point).sub(center).div(radius)
        return HitResult(t, point, normal, material)
    }
}

private fun calculateColor(u: Float, v: Float): color {
    val ray = camera.ray(u, v)
    val result = scene.hit(ray, 0f, Float.MAX_VALUE)
    return if (result != null) {
        color().red()
    } else {
        background[u.toInt() + v.toInt() * RESOLUTION_WIDTH]
    }
}

private fun updateSceneIfNeeded() {
    if (sceneVersion.check()) {
        runBlocking {
            currentJob.cancel()
            regionMutex.withLock { regionsDone.clear() }
            camera.updateBasis()
            currentJob = launch {
                updateRegions(regionsLow)
                if (isActive) {
                    updateRegions(regionsMed)
                }
                if (isActive) {
                    updateRegions(regionsHgh)
                }
            }
        }
    }
}

private suspend fun updateRegions(regions: List<RegionTask>) {
    for (task in regions) {
        withContext(Dispatchers.Default) {
            val result = updateRegion(task)
            if (isActive) {
                regionMutex.withLock { regionsDone.add(result) }
            }
        }
    }
}

private fun partiallyUpdateViewport(left: Long) {
    var time = left
    runBlocking {
        regionMutex.withLock {
            while (time > 0 && regionsDone.isNotEmpty()) {
                val elapsed = measureNanoTime {
                    updateViewportTexture(regionsDone.removeAt(0))
                }
                time -= elapsed
            }
        }
    }
}

private fun updateRegion(regionTask: RegionTask): RegionTask {
    check(REGION_WIDTH % regionTask.uStep == 0 && regionTask.uStep <= REGION_WIDTH)
    check(REGION_HEIGHT % regionTask.vStep == 0 && regionTask.vStep <= REGION_HEIGHT)
    val uHalf = regionTask.uStep / 2f
    val vHalf = regionTask.vStep / 2f
    val uRange = 0 until REGION_WIDTH
    val vRange = 0 until REGION_HEIGHT
    for (v in vRange step regionTask.vStep) {
        for (u in uRange step  regionTask.uStep) {
            val color = calculateColor(regionTask.uFrom + u + uHalf, regionTask.vFrom + v + vHalf)
            fillRegion(u, v, regionTask.uStep, regionTask.vStep, color, regionTask.floatBuffer)
        }
    }
    return regionTask
}

private fun updateViewportTexture(regionTask: RegionTask) {
    viewportTexture.updatePixels(uOffset = regionTask.uFrom, vOffset = regionTask.vFrom, width = REGION_WIDTH, height = REGION_HEIGHT,
            format = backend.GL_RGB, type = backend.GL_FLOAT, pixels = regionTask.byteBuffer)
}

private fun fillRegion(fromU: Int, fromV: Int, width: Int, height: Int, color: color, buffer: FloatBuffer) {
    val uRange = fromU until fromU + width
    val vRange = fromV until fromV + height
    for (v in vRange) {
        for (u in uRange) {
            val offset = (u + v * REGION_WIDTH) * PIXEL_SIZE
            buffer.position(offset)
            buffer.put(color)
        }
    }
}

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        createBackground()
        createRegionTasks()
        viewportTexture = GlTexture(
                unit = 0,
                width = RESOLUTION_WIDTH, height = RESOLUTION_HEIGHT,
                internalFormat = backend.GL_RGB, pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        viewportRect = GlMesh.rect()
        simpleTechnique.create(shadersLib)
    }

    override fun onTick() {
        val elapsed = measureNanoTime {
            GlState.clear()
            controller.apply { position, direction ->
                camera.position.set(position)
                camera.direction.set(direction)
            }
            updateSceneIfNeeded()
            GlState.drawWithNoCulling {
                simpleTechnique.draw(viewM, projectionM) {
                    simpleTechnique.instance(viewportRect, viewportTexture, modelM)
                }
            }
        }
        partiallyUpdateViewport(NANOS_PER_FRAME - elapsed)
    }

    override fun onCursorDelta(delta: vec2) {
        if (mouseControl) {
            wasdInput.onCursorDelta(delta)
            sceneVersion.increment()
        }
    }

    override fun mouseBtnPressed(btn: Int) {
        mouseControl = true
    }

    override fun mouseBtnReleased(btn: Int) {
        mouseControl = false
    }

    override fun keyPressed(key: Int) {
        wasdInput.keyPressed(key)
        sceneVersion.increment()
    }

    override fun keyReleased(key: Int) {
        wasdInput.keyReleased(key)
        sceneVersion.increment()
    }
}

fun main() {
    window.show()
}