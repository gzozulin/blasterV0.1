package com.blaster

import com.blaster.scene.Vec3

data class HitRecord(var t: Float, val point: Vec3, val u:Float, val v: Float, var normal: Vec3, val material: Material)