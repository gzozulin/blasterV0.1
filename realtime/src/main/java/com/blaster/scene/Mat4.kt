package com.blaster.scene

import android.opengl.Matrix

class Mat4(val values: FloatArray) {
    constructor() : this(floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
    ))

    fun frustumInplace(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.frustumM(values, 0, left, right, bottom, top, near, far)
    }

    fun perspectiveInplace(fovyDegrees: Float, aspect: Float, zNear: Float, zFar: Float) {
        Matrix.perspectiveM(values, 0, fovyDegrees, aspect, zNear, zFar)
    }

    fun lookAtInplace(eye: Vec3, center: Vec3, up: Vec3) {
        Matrix.setLookAtM(values, 0,
                eye.value.x, eye.value.y, eye.value.z,
                center.value.x, center.value.y, center.value.z,
                up.value.x, up.value.y, up.value.z
        )
    }

    fun rotateInplace(radians: Float, axis: Vec3) {
        Matrix.rotateM(values, 0, radians, axis.value.x, axis.value.y, axis.value.z)
    }

    fun translateInplace(vec: Vec3) {
        Matrix.translateM(values, 0, vec.value.x, vec.value.y, vec.value.z)
    }

    fun scaleInplace(vec: Vec3) {
        Matrix.scaleM(values, 0, vec.value.x, vec.value.y, vec.value.z)
    }

    operator fun times(other: Mat4): Mat4 {
        val result = FloatArray(16)
        Matrix.multiplyMM(result, 0, values, 0, other.values, 0)
        return Mat4(result)
    }
}