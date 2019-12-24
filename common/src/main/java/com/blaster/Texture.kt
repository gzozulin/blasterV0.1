package com.blaster

import com.blaster.scene.Vec3

interface Texture {
    fun value(u: Float, v: Float, point: Vec3): Vec3
}