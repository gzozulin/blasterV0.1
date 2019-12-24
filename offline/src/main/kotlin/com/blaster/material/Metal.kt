package com.blaster.material

import com.blaster.HitRecord
import com.blaster.Material
import com.blaster.ScatterResult
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.math.randomInUnitSphere
import com.blaster.math.reflect

data class Metal(val albedo: Vec3, val fuzz: Float) : Material {
    private var actualFuzz = if (fuzz <= 1f) fuzz else 1f

    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? {
        val reflected = reflect(ray.direction.makeUnit(), hit.normal)
        val scattered = Ray(hit.point, reflected + randomInUnitSphere() * actualFuzz)
        if (scattered.direction.dot(hit.normal) > 0f) {
            return ScatterResult(albedo, scattered)
        }
        return null
    }

    override fun emitted(u: Float, v: Float, point: Vec3) = Vec3()
}