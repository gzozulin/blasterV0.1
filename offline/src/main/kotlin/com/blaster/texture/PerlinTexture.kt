package com.blaster.texture

import com.blaster.Texture
import com.blaster.math.Perlin
import com.blaster.math.Vec3

class PerlinTexture : Texture {
    private val perlin = Perlin()

    override fun value(u: Float, v: Float, point: Vec3): Vec3 {
        return Vec3(1f) * perlin.noise(point)
    }
}