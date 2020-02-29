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

private const val VIEWPORT_WIDTH = 256
private const val VIEWPORT_HEIGHT = 256

private const val VIEWPORT_LEFT = -VIEWPORT_WIDTH / 2f
private const val VIEWPORT_BOTTOM = -VIEWPORT_HEIGHT / 2f

private const val FOCUS_DISTANCE = 10.0f

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

    fun ray(x: Float, y: Float): ray {
        val u = VIEWPORT_LEFT + x
        val v = VIEWPORT_BOTTOM + y
        val direction = vec3(direction).mul(FOCUS_DISTANCE)
        val uComp = vec3(basisX).mul(u)
        val vComp = vec3(basisY).mul(v)
        direction.add(uComp).add(vComp)
        direction.normalize()
        return ray(position, direction)
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

private val controller = Controller()
private val wasdInput = WasdInput(controller)

private lateinit var viewportRect: GlMesh
private lateinit var viewportTexture: GlTexture
private val viewportBuffer = ByteBuffer
        .allocateDirect(VIEWPORT_WIDTH * VIEWPORT_HEIGHT * 3 * 4)
        .order(ByteOrder.nativeOrder())

private val simpleTechnique = SimpleTechnique()

private val scene = HitableSphere(vec3(0f, 0f, -20f), 19.9f, RtrMaterial())

private fun calculateColor(x: Float, y: Float): color {
    val ray = camera.ray(x, y)
    val result = scene.hit(ray, 0f, Float.MAX_VALUE)
    return if (result != null) {
        color().red()
    } else {
        color().blue()
    }
}

private fun fillRegion(xOffset: Int, yOffset: Int, width: Int, height: Int, fullWidth: Int, color: color, buffer: FloatBuffer) {

    var offset = (xOffset + yOffset * fullWidth) * 3

    for (y in 0 until height) {
        for (x in 0 until width) {
            buffer.position(offset)
            buffer.put(color)
            offset += 3
        }
        offset -= fullWidth * 3
    }
}

private fun updateRegion(xOffset: Int, yOffset: Int, regWidth: Int, regHeight: Int, xStep: Int, yStep: Int) {

    val floatBuffer = viewportBuffer.asFloatBuffer() // constant size for all ops

    check(regWidth % xStep == 0 && xStep <= regWidth)
    check(regHeight % yStep == 0 && yStep <= regHeight)

    val xHalf = xStep / 2f
    val yHalf = yStep / 2f

    val xFrom = xOffset
    val xTo = xOffset + regWidth

    val yFrom = yOffset + regHeight - 1
    val yTo = yOffset

    for (y in yFrom downTo yTo step yStep) {
        for (x in xFrom until xTo step  xStep) {
            val color = calculateColor(x + xHalf, y + yHalf)
            fillRegion(x, y, xStep, yStep, regWidth, color, floatBuffer)
        }
    }
    viewportTexture.updatePixels(xoffset = xOffset, yoffset = yOffset, width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT,
            format = backend.GL_RGB, type = backend.GL_FLOAT, pixels = viewportBuffer)
}

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        viewportTexture = GlTexture(
                unit = 0,
                width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT,
                internalFormat = backend.GL_RGB, pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        viewportRect = GlMesh.rect()
        simpleTechnique.create(shadersLib)
        renderScene()
    }

    private fun renderScene() {
        camera.updateBasis()
        updateRegion(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, 8, 8)
    }

    override fun onTick() {
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