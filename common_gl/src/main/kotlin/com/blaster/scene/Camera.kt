package com.blaster.scene

import com.blaster.common.Version
import com.blaster.common.AABB
import com.blaster.common.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Camera(aspectRatio: Float) : Movable {
    val projectionM = Matrix4f()

    init {
        projectionM.perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 1f, 4000f)
    }

    private val internalPosition: Vector3f = Vector3f()
    private val internalRotation: Quaternionf = Quaternionf()

    private var viewVersion = Version()
    private val viewM = Matrix4f()

    val position
        get() = internalPosition

    private val negatedBuf = Vector3f()
    fun calculateViewM(): Matrix4f {
        if (viewVersion.check()) {
            internalPosition.negate(negatedBuf)
            viewM.identity().rotate(internalRotation).translate(negatedBuf)
        }
        return viewM
    }

    private val directionBuf = Vector3f()
    fun lookAt(from: Vector3f, to: Vector3f) {
        viewVersion.increment()
        internalPosition.set(from)
        to.sub(from, directionBuf).normalize()
        internalRotation.lookAlong(directionBuf, VECTOR_UP)
    }

    fun lookAt(aabb: AABB) {
        viewVersion.increment()
        var maxValue = aabb.width
        if (aabb.height > maxValue) {
            maxValue = aabb.height
        }
        if (aabb.depth > maxValue) {
            maxValue = aabb.depth
        }
        val center = aabb.center
        center.add(Vector3f(0f, maxValue / 2f, maxValue), internalPosition)
        lookAt(internalPosition, center)
    }
}