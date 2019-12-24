package com.blaster

import com.blaster.scene.Ray
import com.blaster.scene.Vec3

data class ScatterResult(val attenuation: Vec3, val scattered: Ray)