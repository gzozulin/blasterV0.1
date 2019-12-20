package com.blaster.math

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
                eye.values[0], eye.values[1], eye.values[2],
                center.values[0], center.values[1], center.values[2],
                up.values[0], up.values[1], up.values[2]
        )
    }

    fun rotateInplace(radians: Float, axis: Vec3) {
        Matrix.rotateM(values, 0, radians, axis.values[0], axis.values[1], axis.values[2])
    }

    fun translateInplace(vec: Vec3) {
        Matrix.translateM(values, 0, vec.values[0], vec.values[1], vec.values[2])
    }

    fun scaleInplace(vec: Vec3) {
        Matrix.scaleM(values, 0, vec.values[0], vec.values[1], vec.values[2])
    }

    operator fun times(other: Mat4): Mat4 {
        val result = FloatArray(16)
        Matrix.multiplyMM(result, 0, values, 0, other.values, 0)
        return Mat4(result)
    }
}