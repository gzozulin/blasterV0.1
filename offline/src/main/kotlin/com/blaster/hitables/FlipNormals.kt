package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.scene.Aabb
import com.blaster.scene.Ray

data class FlipNormals(val hitable: Hitable) : Hitable {
    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        val hit = hitable.hit(ray, tMin, tMax)
        if (hit != null) {
            hit.normal = hit.normal.negate()
        }
        return hit
    }

    override fun aabb(): Aabb = hitable.aabb()
}