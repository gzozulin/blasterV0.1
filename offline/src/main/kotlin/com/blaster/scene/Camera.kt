package com.blaster.scene

import kotlin.math.tan

data class Camera(
    val eye: Vec3, val center: Vec3, val up: Vec3,
    val vfov: Float, val aspect: Float,
    val aperture: Float, val focusDist: Float
) {
    private val lensRadius: Float = aperture / 2f

    private val lowerLeft: Vec3
    private val horizontal: Vec3
    private val vertical: Vec3
    private val origin: Vec3

    private val w: Vec3
    private val u: Vec3
    private val v: Vec3

    init {
        val theta = vfov * Math.PI / 180f
        val halfHeight = tan(theta / 2f).toFloat()
        val halfWidth = aspect * halfHeight
        origin = eye
        w = (eye - center).makeUnit()
        u = up.cross(w).makeUnit()
        v = w.cross(u)
        lowerLeft = origin - u * halfWidth * focusDist - v * halfHeight * focusDist - w * focusDist
        horizontal = u * halfWidth * focusDist * 2f
        vertical = v * halfHeight * focusDist * 2f
    }

    fun getRay(s: Float, t: Float): Ray {
        val rd = Vec3.randomInUnitDisk() * lensRadius
        val offset = u * rd.x + v * rd.y
        return Ray(origin + offset, lowerLeft + horizontal * s + vertical * t - origin - offset)
    }
}