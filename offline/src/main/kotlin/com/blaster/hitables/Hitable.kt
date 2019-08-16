package com.blaster.hitables

import com.blaster.math.AABB
import com.blaster.tracing.HitRecord
import com.blaster.math.Ray

interface Hitable {
    fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord?
    fun aabb(): AABB
}