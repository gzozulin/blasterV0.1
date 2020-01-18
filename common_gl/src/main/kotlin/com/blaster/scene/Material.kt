package com.blaster.scene

import com.blaster.common.vec3

// This is a placeholder

data class Material(val ambient: vec3, val diffuse: vec3, val specular: vec3, val shine: Float) {
    companion object {
        val CONCRETE = Material(vec3(0.329412f, 0.223529f, 0.027451f), vec3(0.75f, 0.75f, 0.73f), vec3(0f, 0f, 0f), 1f)
        val BRASS = Material(vec3(0.329412f, 0.223529f, 0.027451f), vec3(0.780392f, 0.568627f, 0.113725f), vec3(0.992157f, 0.941176f, 0.807843f), 27.8974f)
        val BRONZE = Material(vec3(0.2125f, 0.1275f, 0.054f), vec3(0.714f, 0.4284f, 0.18144f), vec3(0.393548f, 0.271906f, 0.166721f), 25.6f)
        val POLISHED_BRONZE = Material(vec3(0.25f, 0.148f, 0.06475f), vec3(0.4f, 0.2368f, 0.1036f), vec3(0.774597f, 0.458561f, 0.200621f), 76.8f)
        val CHROME = Material(vec3(0.25f, 0.25f, 0.25f), vec3(0.4f, 0.4f, 0.4f), vec3(0.774597f, 0.774597f, 0.774597f), 76.8f)
        // todo: etc
    }
}