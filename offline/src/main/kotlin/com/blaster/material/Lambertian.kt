package com.blaster.material

import com.blaster.tracing.HitRecord
import com.blaster.tracing.ScatterResult
import com.blaster.texture.Texture
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.math.randomInUnitSphere

data class Lambertian(val albedo: Texture) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? =
        ScatterResult(
            albedo.value(hit.u, hit.v, hit.point),
                Ray(hit.point, hit.point + hit.normal + randomInUnitSphere() - hit.point)
        )

    override fun emitted(u: Float, v: Float, point: Vec3) = Vec3()
}