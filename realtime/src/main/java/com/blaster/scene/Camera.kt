package com.blaster.scene

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(aspectRatio: Float) {
    val viewM = Matrix4f()
    val projectionM = Matrix4f()

    var eye = Vector3f()
    var center = Vector3f()

    init {
        projectionM.perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 1f, 4000f)
    }

    fun lookAt(from: Vector3f, to: Vector3f) {
        viewM.lookAt(from, to, Vector3f(0f, 1f, 0f))
        eye = from
        center = to
    }

    fun lookAt(aabb: AABB) {
        var maxValue = aabb.width
        if (aabb.height > maxValue) {
            maxValue = aabb.height
        }
        if (aabb.depth > maxValue) {
            maxValue = aabb.depth
        }
        val center = aabb.center
        val result = Vector3f()
        center.add(Vector3f(0f, maxValue / 2f, maxValue), result)
        lookAt(result, center)
    }
}