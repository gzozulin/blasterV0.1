package com.blaster

import com.blaster.math.Aabb
import com.blaster.math.Ray

interface Hitable {
    fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord?
    fun aabb(): Aabb
}