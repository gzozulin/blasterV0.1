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
import org.joml.Vector2f
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

private val backend = GlLocator.locate()

private const val VIEWPORT_WIDTH = 300
private const val VIEWPORT_HEIGHT = 200

private const val VIEWPORT_L = -VIEWPORT_WIDTH / 2f
private const val VIEWPORT_R = VIEWPORT_WIDTH / 2f
private const val VIEWPORT_B = -VIEWPORT_HEIGHT / 2f
private const val VIEWPORT_T = VIEWPORT_HEIGHT / 2f

private const val FOCUS_DISTANCE = 1.0f

private class RtrMaterial

private class HitResult(t: Float, point: vec3, normal: vec3, material: RtrMaterial)

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

    fun ray(i: Int, j: Int): ray {
        val u = VIEWPORT_L + (VIEWPORT_R - VIEWPORT_L) * (i + 0.5f) / VIEWPORT_WIDTH
        val v = VIEWPORT_B + (VIEWPORT_T - VIEWPORT_B) * (j + 0.5f) / VIEWPORT_HEIGHT
        val dir = vec3(direction).mul(FOCUS_DISTANCE)
        val uComp = vec3(basisX).mul(u)
        val vComp = vec3(basisY).mul(v)
        return ray(position, dir.add(uComp).add(vComp))
    }
}

private interface Hitable {
    fun hit(ray: ray, t0: Float, t1: Float): HitResult?
}

private class Sphere(private val center: vec3, private val radius: Float, private val material: RtrMaterial) : Hitable {
    override fun hit(ray: ray, t0: Float, t1: Float): HitResult? {
        val ec = ray.origin().sub(center)
        val d = ray.dir()
        val d2 = d.dot(d)
        val discriminant = powf(d.dot(ec), 2f) - d2 * (ec.dot(ec) - powf(radius, 2f))
        if (discriminant > 0) {
            val sqrtDiscriminant = sqrt(discriminant)
            val b2 = -d.dot(ec)
            var t = (b2 - sqrtDiscriminant) / d2
            if (t in t0..t1) {
                return hitResult(t, ray)
            }
            t = (b2 + sqrtDiscriminant) / d2
            if (t in t0..t1) {
                return hitResult(t, ray)
            }
        }
        return null
    }

    private fun hitResult(t: Float, ray: ray): HitResult {
        val point = ray.pointAt(t)
        val normal = vec3(point).sub(center).div(radius)
        return HitResult(t, point, normal, material)
    }
}

private val upVec = vec3().up()
private val viewM = mat4().identity()
private val projectionM = mat4().ortho(-1f, 1f, -1f, 1f, 1f, -1f)
private val modelM = mat4().identity()

private var mouseControl = false

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)

private val camera = RtrCamera()

private val controller = Controller()
private val wasdInput = WasdInput(controller)

private lateinit var viewportRect: GlMesh
private lateinit var viewportTexture: GlTexture
private val viewportBuffer = ByteBuffer.allocateDirect(VIEWPORT_WIDTH * VIEWPORT_HEIGHT * 3).order(ByteOrder.nativeOrder())

private val simpleTechnique = SimpleTechnique()

private val scene = Sphere(vec3(0f, 0f, -2f), 1f, RtrMaterial())

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        viewportTexture = GlTexture(
                unit = 0,
                width = VIEWPORT_WIDTH, height = VIEWPORT_HEIGHT,
                internalFormat = backend.GL_RGB, pixelFormat = backend.GL_RGB, pixelType = backend.GL_UNSIGNED_BYTE)
        viewportRect = GlMesh.rect()
        simpleTechnique.create(shadersLib)
        renderScene()
    }

    private fun renderScene() {
        viewportBuffer.rewind()
        camera.updateBasis()
        for (i in 0 until VIEWPORT_WIDTH) {
            for (j in 0 until VIEWPORT_HEIGHT) {
                val ray = camera.ray(i, j)
                val result = scene.hit(ray, 0f, Float.MAX_VALUE)
                if (result != null) {
                    viewportBuffer.put(127)
                    viewportBuffer.put(0)
                    viewportBuffer.put(0)
                } else {
                    viewportBuffer.put(0)
                    viewportBuffer.put(0)
                    viewportBuffer.put(0)
                }
            }
        }
        viewportBuffer.position(0)
        viewportTexture.updatePixels(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, backend.GL_RGB, backend.GL_UNSIGNED_BYTE, viewportBuffer)
    }

    override fun onTick() {
        renderScene()
        GlState.drawWithNoCulling {
            simpleTechnique.draw(viewM, projectionM) {
                simpleTechnique.instance(viewportRect, viewportTexture, modelM)
            }
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
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