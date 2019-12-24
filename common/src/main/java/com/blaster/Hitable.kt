package com.blaster

import com.blaster.scene.Aabb
import com.blaster.scene.Ray

interface Hitable {
    fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord?
    fun aabb(): Aabb
}