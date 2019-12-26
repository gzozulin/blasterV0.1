package com.blaster.material

import com.blaster.HitRecord
import com.blaster.Material
import com.blaster.ScatterResult
import com.blaster.Texture
import com.blaster.scene.Ray
import com.blaster.scene.Vec3

data class DiffuseMaterial(val texture: Texture) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? = null

    override fun emitted(u: Float, v: Float, point: Vec3) = texture.value(u, v, point)
}