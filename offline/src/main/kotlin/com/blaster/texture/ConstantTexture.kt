package com.blaster.texture

import com.blaster.Texture
import com.blaster.scene.Vec3

data class ConstantTexture(val color: Vec3) : Texture {
    override fun value(u: Float, v: Float, point: Vec3) = color
}