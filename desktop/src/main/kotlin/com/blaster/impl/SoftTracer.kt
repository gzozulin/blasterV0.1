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

private val backend = GlLocator.locate()

private const val VIEWPORT_WIDTH = 10
private const val VIEWPORT_HEIGHT = 10

private const val FOCUS_DISTANCE = 10.0f

private const val VIEWPORT_LEFT = (-VIEWPORT_WIDTH / 2f).toInt()
private const val VIEWPORT_BOTTOM = (-VIEWPORT_HEIGHT / 2f).toInt()

private const val RESOLUTION_WIDTH = 1024
private const val RESOLUTION_HEIGHT = 768

private const val REGION_WIDTH = 32
private const val REGION_HEIGHT = 32

private const val REGIONS_CNT_U = RESOLUTION_WIDTH / REGION_WIDTH
private const val REGIONS_CNT_V = RESOLUTION_HEIGHT / REGION_HEIGHT

private const val REGIONS_CNT = REGIONS_CNT_U * REGIONS_CNT_V

private const val PIXEL_SIZE = 3 // r, g, b

private const val REGIONS_PER_TICK = 50

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

private val regions = mutableListOf<RegionTask>()
private val regionsDone = mutableListOf<RegionTask>()
private val regionMutex = Mutex()

private var currentJob: Job = Job()

private class RegionTask(index: Int, val uStep: Int, val vStep: Int) {
    val byteBuffer: ByteBuffer = ByteBuffer
            .allocateDirect(REGION_WIDTH * REGION_HEIGHT * PIXEL_SIZE * 4)
            .order(ByteOrder.nativeOrder())
    val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer()

    val uFrom = (index % REGIONS_CNT_U) * REGION_WIDTH
    val vFrom = (index / REGIONS_CNT_U) * REGION_HEIGHT
}

private fun createRegionTasks() {
    for (i in 0 until REGIONS_CNT) {
        regions.add(RegionTask(i, 32, 32))
    }
    for (i in 0 until REGIONS_CNT) {
        regions.add(RegionTask(i, 8, 8))
    }
    for (i in 0 until REGIONS_CNT) {
        regions.add(RegionTask(i, 1, 1))
    }
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
        color().blue()
    }
}

private fun updateSceneIfNeeded() {
    if (sceneVersion.check()) {
        runBlocking {
            currentJob.cancel()
            regionMutex.withLock {
                regionsDone.clear()
            }
            camera.updateBasis()
            currentJob = launch {
                for (task in regions) {
                    withContext(Dispatchers.Default) {
                        val result = updateRegion(task)
                        if (isActive) {
                            regionMutex.withLock {
                                regionsDone.add(result)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun partiallyUpdateViewport() {
    runBlocking {
        regionMutex.withLock {
            for (i in 0 until REGIONS_PER_TICK) {
                if (regionsDone.isEmpty()) {
                    break
                }
                val first = regionsDone.removeAt(0)
                updateViewportTexture(first)
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
        createRegionTasks()
        viewportTexture = GlTexture(
                unit = 0,
                width = RESOLUTION_WIDTH, height = RESOLUTION_HEIGHT,
                internalFormat = backend.GL_RGB, pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        viewportRect = GlMesh.rect()
        simpleTechnique.create(shadersLib)
    }

    override fun onTick() {
        GlState.clear()
        controller.apply { position, direction ->
            camera.position.set(position)
            camera.direction.set(direction)
        }
        updateSceneIfNeeded()
        partiallyUpdateViewport()
        GlState.drawWithNoCulling {
            simpleTechnique.draw(viewM, projectionM) {
                simpleTechnique.instance(viewportRect, viewportTexture, modelM)
            }
        }
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