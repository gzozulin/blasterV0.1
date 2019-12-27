package com.blaster.hitables

import com.blaster.material.Material
import com.blaster.math.Vec3

data class HitRecord(var t: Float, val point: Vec3, val u:Float, val v: Float, var normal: Vec3, val material: Material)