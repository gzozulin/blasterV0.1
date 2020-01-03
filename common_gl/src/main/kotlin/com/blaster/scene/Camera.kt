package com.blaster.scene

import com.blaster.math.AABB
import com.blaster.math.VECTOR_UP
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

    private var localMatrixVersion = 0
    private var localMatrixLastVersion = Int.MAX_VALUE
    private val localM = Matrix4f()

    val position
        get() = internalPosition

    private val negatedBuf = Vector3f()
    fun calculateViewM(): Matrix4f {
        if (localMatrixVersion != localMatrixLastVersion) {
            internalPosition.negate(negatedBuf)
            localM.identity().rotate(internalRotation).translate(negatedBuf)
            localMatrixLastVersion = localMatrixVersion
        }
        return localM
    }

    private val directionBuf = Vector3f()
    fun lookAt(from: Vector3f, to: Vector3f) {
        localMatrixLastVersion++
        internalPosition.set(from)
        to.sub(from, directionBuf).normalize()
        internalRotation.lookAlong(directionBuf, VECTOR_UP)
    }

    fun lookAt(aabb: AABB) {
        localMatrixLastVersion++
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