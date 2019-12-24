package com.blaster

import com.blaster.math.Vec3

interface Texture {
    fun value(u: Float, v: Float, point: Vec3): Vec3
}