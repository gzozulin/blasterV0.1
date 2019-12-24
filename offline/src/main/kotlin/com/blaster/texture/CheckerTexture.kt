package com.blaster.texture

import com.blaster.Texture
import com.blaster.scene.Vec3
import kotlin.math.sin

data class CheckerTexture(val odd: Texture, val even: Texture) : Texture {
    override fun value(u: Float, v: Float, point: Vec3): Vec3 {
        val sines = sin(10f * point.x) * sin(10f * point.y) * sin(10f * point.z)
        return if (sines < 0f) odd.value(u,v, point) else even.value(u, v, point)
    }
}