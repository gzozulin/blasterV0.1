package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.Material
import com.blaster.math.Aabb
import com.blaster.math.Ray
import com.blaster.math.Vec3
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

data class Sphere(val center: Vec3, val radius: Float, val material: Material) : Hitable {

    private val aabb = Aabb(center - Vec3(radius), center + Vec3(radius))

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        if (!aabb.hit(ray, tMin, tMax)) {
            return null
        }
        val oc = ray.origin - center
        val a = ray.direction.dot(ray.direction)
        val b = oc.dot(ray.direction) * 2f
        val c = oc.dot(oc) - radius * radius
        val discriminant = b * b - (4f * a * c)
        if (discriminant > 0) {
            val doubleA = (2f * a)
            val sqrtfDisc = sqrt(discriminant)
            var t = (-b - sqrtfDisc) / doubleA
            if (t < tMax && t > tMin) {
                return hitRecord(ray, t)
            }
            t = (-b + sqrtfDisc) / doubleA
            if (t < tMax && t > tMin) {
                return hitRecord(ray, t)
            }
        }
        return null
    }

    override fun aabb() = aabb

    private fun hitRecord(ray: Ray, t: Float): HitRecord {
        val pointAtParameter = ray.pointAtParameter(t)
        val uv = getSphereUV((pointAtParameter - center) / radius)
        return HitRecord(
                t,
                pointAtParameter,
                uv.first,
                uv.second,
                (pointAtParameter - center) / radius,
                material
        )
    }
}

fun getSphereUV(point: Vec3) : Pair<Float, Float> {
    val phi = atan2(point.z, point.x)
    val theta = asin(point.y)
    val u = 1f - (phi + Math.PI.toFloat()) / (2f * Math.PI.toFloat())
    val v = (theta + Math.PI.toFloat() / 2f) / Math.PI.toFloat()
    return Pair(u, v)
}