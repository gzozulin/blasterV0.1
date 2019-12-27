package com.blaster.material

import com.blaster.hitables.HitRecord
import com.blaster.texture.Texture
import com.blaster.math.Ray
import com.blaster.math.Vec3

data class DiffuseMaterial(val texture: Texture) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? = null

    override fun emitted(u: Float, v: Float, point: Vec3) = texture.value(u, v, point)
}