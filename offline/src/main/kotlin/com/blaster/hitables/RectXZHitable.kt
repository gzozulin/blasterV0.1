package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.Material
import com.blaster.math.Aabb
import com.blaster.math.Ray
import com.blaster.math.Vec3

data class RectXZHitable(
    val x0: Float, val x1: Float,
    val z0: Float, val z1: Float,
    val k: Float,
    val material: Material
) : Hitable {

    private val aabb = Aabb(Vec3(x0, k - 0.0001f, z0), Vec3(x1, k + 0.0001f, z1))

    override fun aabb(): Aabb = aabb

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        val t = (k - ray.origin.y) / ray.direction.y
        if (t < tMin || t > tMax) {
            return null
        }
        val x = ray.origin.x + t * ray.direction.x
        val z = ray.origin.z + t * ray.direction.z
        if (x < x0 || x > x1 || z < z0 || z > z1) {
            return null
        }
        val u = (x - x0) / (x1 - x0)
        val v = (z - z0) / (z1 - z0)
        return HitRecord(t, ray.pointAtParameter(t), u, v, Vec3(0f, 1f, 0f), material)
    }
}