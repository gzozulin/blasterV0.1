package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.Material
import com.blaster.scene.Aabb
import com.blaster.scene.Ray
import com.blaster.scene.Vec3

data class RectXY(
    val x0: Float, val x1: Float,
    val y0: Float, val y1: Float,
    val k: Float,
    val material: Material
) : Hitable {

    private val aabb = Aabb(Vec3(x0, y0, k - 0.0001f), Vec3(x1, y1, k + 0.0001f))

    override fun aabb(): Aabb = aabb

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        val t = (k - ray.origin.z) / ray.direction.z
        if (t < tMin || t > tMax) {
            return null
        }
        val x = ray.origin.x + t * ray.direction.x
        val y = ray.origin.y + t * ray.direction.y
        if (x < x0 || x > x1 || y < y0 || y > y1) {
            return null
        }
        val u = (x - x0) / (x1 - x0)
        val v = (y - y0) / (y1 - y0)
        return HitRecord(t, ray.pointAtParameter(t), u, v, Vec3(0f, 0f, 1f), material)
    }
}