package com.blaster.material

import com.blaster.HitRecord
import com.blaster.Material
import com.blaster.ScatterResult
import com.blaster.Texture
import com.blaster.math.Ray
import com.blaster.math.Vec3

data class LambertianMaterial(val albedo: Texture) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? =
        ScatterResult(
            albedo.value(hit.u, hit.v, hit.point),
                Ray(hit.point, hit.point + hit.normal + Vec3.randomInUnitSphere() - hit.point)
        )

    override fun emitted(u: Float, v: Float, point: Vec3) = Vec3()
}