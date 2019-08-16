package com.blaster.math

data class Ray(var origin: Vec3, var direction: Vec3) {
    fun pointAtParameter(t: Float) = origin + (direction * t)
}