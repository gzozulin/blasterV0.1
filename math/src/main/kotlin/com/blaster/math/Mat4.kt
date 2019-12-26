package com.blaster.math

import org.joml.Matrix4f

data class Mat4(val value: Matrix4f = Matrix4f()) {
    fun perspective(fovyDegrees: Float, aspect: Float, zNear: Float, zFar: Float) {
        value.perspective(fovyDegrees, aspect, zNear, zFar)
    }

    fun lookAt(eye: Vec3, center: Vec3, up: Vec3) {
        value.lookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z)
    }

    fun rotate(radians: Float, axis: Vec3) {
        value.rotate(radians, axis.x, axis.y, axis.z)
    }

    operator fun times(other: Mat4): Mat4 = Mat4(value.mul(other.value))
}