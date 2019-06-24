package com.gzozulin.wallpaper.gl

import android.opengl.Matrix

data class Matrix4f(val values: FloatArray) {
    constructor() : this(floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
    ))

    fun makeFrustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.frustumM(values, 0, left, right, bottom, top, near, far)
    }

    fun makePerspective() {
        check(false) { "Implement me!" }
    }

    fun makeLookAt(eye: Vector4f, center: Vector4f, up: Vector4f) {
        Matrix.setLookAtM(values, 0,
                eye.values[0], eye.values[1], eye.values[2],
                center.values[0], center.values[1], center.values[2],
                up.values[0], up.values[1], up.values[2]
        )
    }

    operator fun times(other: Matrix4f): Matrix4f {
        val result = FloatArray(16)
        Matrix.multiplyMM(result, 0, values, 0, other.values, 0)
        return Matrix4f(result)
    }
}