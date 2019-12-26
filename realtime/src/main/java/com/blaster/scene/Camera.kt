package com.blaster.scene

import com.blaster.math.Aabb
import com.blaster.math.Mat4
import com.blaster.math.Vec3

class Camera(aspectRatio: Float) {
    val viewM = Mat4()
    val projectionM = Mat4()

    var eye = Vec3()
    var center = Vec3()

    init {
        projectionM.perspective(90f, aspectRatio, 1f, 4000f)
    }

    fun lookAt(from: Vec3, to: Vec3) {
        viewM.lookAt(from, to, Vec3(y = 1f))
        eye = from
        center = to
    }

    fun lookAt(aabb: Aabb) {
        var maxValue = aabb.width
        if (aabb.height > maxValue) {
            maxValue = aabb.height
        }
        if (aabb.depth > maxValue) {
            maxValue = aabb.depth
        }
        val center = aabb.center
        val from = center + Vec3(y = maxValue / 2f, z = maxValue)
        lookAt(from, center)
    }
}