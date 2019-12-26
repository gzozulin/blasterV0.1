package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.Texture
import com.blaster.material.IsotropicMaterial
import com.blaster.scene.Ray
import com.blaster.scene.Vec3
import java.util.*
import kotlin.math.ln

data class MediumHitable(val boundary: Hitable, val density: Float, val texture: Texture) : Hitable {
    private val phaseFunction = IsotropicMaterial(texture)

    override fun aabb() = boundary.aabb()

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        val hit1 = boundary.hit(ray, Float.MIN_VALUE, Float.MAX_VALUE)
        if (hit1 != null) {
            val hit2 = boundary.hit(ray, hit1.t + 0.0001f, Float.MAX_VALUE)
            if (hit2 != null) {
                if (hit1.t < tMin) {
                    hit1.t = tMin
                }
                if (hit2.t > tMax) {
                    hit2.t = tMax
                }
                if (hit1.t >= hit2.t) {
                    return null
                }
                if (hit1.t < 0f) {
                    hit1.t = 0f
                }
                val distanceInsideBoundary = (hit2.t - hit1.t) * ray.direction.length()
                val hitDistance = -(1f / density) * ln(RANDOM.nextDouble()).toFloat()
                if (hitDistance < distanceInsideBoundary) {
                    val t = hit1.t + hitDistance / ray.direction.length()
                    return HitRecord(
                            t,
                            ray.pointAtParameter(t),
                            0f, 0f,
                            Vec3(1f, 0f, 0f),
                            phaseFunction
                    )
                }
            }
        }
        return null
    }

    private companion object {
        private val RANDOM = Random()
    }
}