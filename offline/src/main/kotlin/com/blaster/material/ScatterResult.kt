package com.blaster.material

import com.blaster.math.Ray
import com.blaster.math.Vec3

data class ScatterResult(val attenuation: Vec3, val scattered: Ray)