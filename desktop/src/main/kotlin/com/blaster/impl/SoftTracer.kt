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
import org.joml.Intersectionf
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private val backend = GlLocator.locate()

private const val VIEWPORT_WIDTH = 10
private const val VIEWPORT_HEIGHT = 10

private const val VIEWPORT_LEFT = (-VIEWPORT_WIDTH / 2f).toInt()
private const val VIEWPORT_BOTTOM = (-VIEWPORT_HEIGHT / 2f).toInt()

private const val FOCUS_DISTANCE = 10.0f

private const val RESOLUTION_WIDTH = 1024
private const val RESOLUTION_HEIGHT = 1024

private const val PIXEL_SIZE = 3

private lateinit var viewportRect: GlMesh
private lateinit var viewportTexture: GlTexture

// todo: static list of predefined jobs (byte buff, float buff, region, steps)

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
        // assuming viewport w and h are equal to the image w and h
        val x = VIEWPORT_LEFT + VIEWPORT_WIDTH * (u + 0.5f) / RESOLUTION_WIDTH
        val y = VIEWPORT_BOTTOM + VIEWPORT_HEIGHT * (v +0.5f) / RESOLUTION_HEIGHT
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

private val upVec = vec3().up()
private val viewM = mat4().identity()
private val projectionM = mat4().identity()
private val modelM = mat4().identity()

private var mouseControl = false

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)

private val camera = RtrCamera()

private val controller = Controller(velocity = 0.1f, position = vec3().back())
private val wasdInput = WasdInput(controller)

private val simpleTechnique = SimpleTechnique()

private val scene = HitableSphere(vec3(0f, 0f, -12f), 2f, RtrMaterial())

private fun calculateColor(u: Float, v: Float): color {
    val ray = camera.ray(u, v)
    val result = scene.hit(ray, 0f, Float.MAX_VALUE)
    return if (result != null) {
        color().red()
    } else {
        color().blue()
    }
}

val byteBuffer = ByteBuffer
        .allocateDirect(RESOLUTION_WIDTH * RESOLUTION_HEIGHT * PIXEL_SIZE * 4)
        .order(ByteOrder.nativeOrder())

val floatBuffer = byteBuffer.asFloatBuffer()

private fun updateRegion(fromU: Int, fromV: Int, width: Int, height: Int, uStep: Int, vStep: Int) {



    check(width % uStep == 0 && uStep <= width)
    check(height % vStep == 0 && vStep <= height)

    val uHalf = uStep / 2f
    val vHalf = vStep / 2f

    val uRange = fromU until fromU + width
    val yRange = fromV until fromV + height

    for (v in yRange step vStep) {
        for (u in uRange step  uStep) {
            val color = calculateColor(u + uHalf, v + vHalf)
            fillRegion(u, v, uStep, vStep, width, color, floatBuffer)
        }
    }
    viewportTexture.updatePixels(uOffset = fromU, vOffset = fromV, width = width, height = height,
            format = backend.GL_RGB, type = backend.GL_FLOAT, pixels = byteBuffer)
}

private fun fillRegion(fromU: Int, fromV: Int, width: Int, height: Int, line: Int, color: color, buffer: FloatBuffer) {
    val uRange = fromU until fromU + width
    val vRange = fromV until fromV + height
    for (v in vRange) {
        for (u in uRange) {
            val offset = (u + v * line) * PIXEL_SIZE
            buffer.position(offset)
            buffer.put(color)
        }
    }
}

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        viewportTexture = GlTexture(
                unit = 0,
                width = RESOLUTION_WIDTH, height = RESOLUTION_HEIGHT,
                internalFormat = backend.GL_RGB, pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        viewportRect = GlMesh.rect()
        simpleTechnique.create(shadersLib)

    }

    private fun renderScene() {
        camera.updateBasis()
        updateRegion(0, 0, RESOLUTION_WIDTH/2, RESOLUTION_HEIGHT/2, 16, 16)
        updateRegion(RESOLUTION_WIDTH/2, RESOLUTION_HEIGHT/2, RESOLUTION_WIDTH/2, RESOLUTION_HEIGHT/2, 1, 1)
    }

    override fun onTick() {
        GlState.clear()
        controller.apply { position, direction ->
            camera.position.set(position)
            camera.direction.set(direction)
        }
        renderScene()
        GlState.drawWithNoCulling {
            simpleTechnique.draw(viewM, projectionM) {
                simpleTechnique.instance(viewportRect, viewportTexture, modelM)
            }
        }
    }

    override fun onCursorDelta(delta: vec2) {
        if (mouseControl) {
            wasdInput.onCursorDelta(delta)
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
    }

    override fun keyReleased(key: Int) {
        wasdInput.keyReleased(key)
    }
}

fun main() {
    window.show()
}