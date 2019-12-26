package com.blaster.material

import com.blaster.HitRecord
import com.blaster.Material
import com.blaster.ScatterResult
import com.blaster.math.Ray
import com.blaster.math.Vec3
import java.util.*
import kotlin.math.pow

data class DielectricMaterial(val reflectionIndex: Float) : Material {
    override fun scattered(ray: Ray, hit: HitRecord): ScatterResult? {
        val reflected = Vec3.reflect(ray.direction, hit.normal)
        val outwardNormal: Vec3
        val niOverNt: Float
        val cosine: Float
        val attenuation = Vec3(1f)
        if (ray.direction.dot(hit.normal) > 0f) {
            outwardNormal = hit.normal.negate()
            niOverNt = reflectionIndex
            cosine = reflectionIndex * ray.direction.dot(hit.normal) / ray.direction.length()
        } else {
            outwardNormal = hit.normal
            niOverNt = 1f / reflectionIndex
            cosine = -ray.direction.dot(hit.normal) / ray.direction.length()
        }
        val refracted = Vec3.refract(ray.direction, outwardNormal, niOverNt)
        if (refracted != null) {
            val reflectionProbe = schlick(cosine, reflectionIndex)
            if (reflectionProbe <= RANDOM.nextFloat()) {
                return ScatterResult(attenuation, Ray(hit.point, refracted))
            }
        }
        return ScatterResult(attenuation, Ray(hit.point, reflected))
    }

    override fun emitted(u: Float, v: Float, point: Vec3): Vec3 = Vec3()

    private fun schlick(cosine: Float, reflectionIndex: Float): Float {
        val r0 = (1 - reflectionIndex) / (1 + reflectionIndex)
        val rSq = r0 * r0
        return rSq + (1 - rSq) * (1f - cosine).toDouble().pow(5.0).toFloat()
    }

    private companion object {
        private val RANDOM = Random()
    }
}