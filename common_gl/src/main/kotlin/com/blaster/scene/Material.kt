package com.blaster.scene

import com.blaster.common.vec3

// This is a placeholder

data class Material(val ambient: vec3, val diffuse: vec3, val specular: vec3, val shine: Float) {
    companion object {
        val BRASS = Material(vec3(0.329412f, 0.223529f, 0.027451f), vec3(0.780392f, 0.568627f, 0.113725f), vec3(0.992157f, 0.941176f, 0.807843f), 27.8974f)
        // todo: etc
    }
}