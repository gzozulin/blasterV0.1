package com.blaster.scene

import com.blaster.common.color
import com.blaster.common.vec3

// This is a placeholder

data class Light(val vector: vec3 = vec3(), val intensity: color, val point: Boolean = true) {
    companion object {
        val SUNLIGHT = Light(vec3(-1f), vec3(3f), point = false)
        val POINT_RED_MINOR = Light(vec3(), vec3(0.1f, 0f, 0f))
        // todo
    }
}