package com.blaster.material

import com.blaster.tracing.HitRecord
import com.blaster.tracing.ScatterResult
import com.blaster.texture.Texture
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.math.randomInUnitSphere

data class Isotropic(var texture: Texture) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? {
        val scattered = Ray(hit.point, randomInUnitSphere())
        return ScatterResult(texture.value(hit.u, hit.v, hit.point), scattered)
    }

    override fun emitted(u: Float, v: Float, point: Vec3)= Vec3()
}