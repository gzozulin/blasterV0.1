package com.blaster

import com.blaster.math.Ray
import com.blaster.math.Vec3

interface Material {
    fun scattered(ray: Ray, hit: HitRecord): ScatterResult?
    fun emitted(u: Float, v: Float, point: Vec3): Vec3
}