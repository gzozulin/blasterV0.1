package com.blaster.scene

import org.joml.Matrix4f

data class Mat4(val value: Matrix4f = Matrix4f()) {
    fun perspective(fovyDegrees: Float, aspect: Float, zNear: Float, zFar: Float) {
        value.perspective(fovyDegrees, aspect, zNear, zFar)
    }

    fun lookAt(eye: Vec3, center: Vec3, up: Vec3) {
        value.lookAt(eye.value, center.value, up.value)
    }

    fun rotate(radians: Float, axis: Vec3) {
        value.rotate(radians, axis.value)
    }

    operator fun times(other: Mat4): Mat4 = Mat4(value.mul(other.value))
}