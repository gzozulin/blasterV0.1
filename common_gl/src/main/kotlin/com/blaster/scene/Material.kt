package com.blaster.scene

import com.blaster.common.vec3

// This is a placeholder

data class Material(val ambient: vec3, val diffuse: vec3, val specular: vec3, val shine: Float) {
    companion object {
        val CONCRETE        = Material(vec3(0.329412f, 0.223529f, 0.027451f),   vec3(0.75f, 0.75f, 0.73f),              vec3(0.01f, 0.01f, 0.01f),                  1f)
        val BRASS           = Material(vec3(0.329412f, 0.223529f, 0.027451f),   vec3(0.780392f, 0.568627f, 0.113725f),  vec3(0.992157f, 0.941176f, 0.807843f),      27.8974f)
        val BRONZE          = Material(vec3(0.2125f, 0.1275f, 0.054f),          vec3(0.714f, 0.4284f, 0.18144f),        vec3(0.393548f, 0.271906f, 0.166721f),      25.6f)
        val POLISHED_BRONZE = Material(vec3(0.25f, 0.148f, 0.06475f),           vec3(0.4f, 0.2368f, 0.1036f),           vec3(0.774597f, 0.458561f, 0.200621f),      76.8f)
        val CHROME          = Material(vec3(0.25f, 0.25f, 0.25f),               vec3(0.4f, 0.4f, 0.4f),                 vec3(0.774597f, 0.774597f, 0.774597f),      76.8f)
        val COPPER          = Material(vec3(0.19125f, 0.0735f, 0.0225f),        vec3(0.7038f, 0.27048f, 0.0828f),       vec3(0.256777f, 0.137622f, 0.086014f),      12.8f)
        val POLISHED_COPPER = Material(vec3(0.2295f, 0.08825f, 0.0275f),        vec3(0.5508f, 0.2118f, 0.066f),         vec3(0.580594f, 0.223257f, 0.0695701f),     51.2f)
        val GOLD            = Material(vec3(0.24725f, 0.1995f, 0.0745f),        vec3(0.75164f, 0.60648f, 0.22648f),     vec3(0.628281f, 0.555802f, 0.366065f),      51.2f)
        // todo: etc
    }
}