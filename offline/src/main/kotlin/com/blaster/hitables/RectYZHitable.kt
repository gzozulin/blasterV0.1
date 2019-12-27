package com.blaster.hitables

import com.blaster.material.Material
import com.blaster.math.Aabb
import com.blaster.math.Ray
import com.blaster.math.Vec3

data class RectYZHitable(
    val y0: Float, val y1: Float,
    val z0: Float, val z1: Float,
    val k: Float,
    val material: Material
) : Hitable {

    private val aabb = Aabb(Vec3(k - 0.0001f, y0, z0), Vec3(k + 0.0001f, y1, z1))

    override fun aabb(): Aabb = aabb

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        val t = (k - ray.origin.x) / ray.direction.x
        if (t < tMin || t > tMax) {
            return null
        }
        val y = ray.origin.y + t * ray.direction.y
        val z = ray.origin.z + t * ray.direction.z
        if (y < y0 || y > y1 || z < z0 || z > z1) {
            return null
        }
        val u = (y - y0) / (y1 - y0)
        val v = (z - z0) / (z1 - z0)
        return HitRecord(t, ray.pointAtParameter(t), u, v, Vec3(1f, 0f, 0f), material)
    }
}