package com.blaster.material

import com.blaster.HitRecord
import com.blaster.Material
import com.blaster.ScatterResult
import com.blaster.scene.Ray
import com.blaster.scene.Vec3

data class Metal(val albedo: Vec3, val fuzz: Float) : Material {
    private var actualFuzz = if (fuzz <= 1f) fuzz else 1f

    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? {
        val reflected = Vec3.reflect(ray.direction.makeUnit(), hit.normal)
        val scattered = Ray(hit.point, reflected + Vec3.randomInUnitSphere() * actualFuzz)
        if (scattered.direction.dot(hit.normal) > 0f) {
            return ScatterResult(albedo, scattered)
        }
        return null
    }

    override fun emitted(u: Float, v: Float, point: Vec3) = Vec3()
}