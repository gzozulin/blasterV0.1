package com.blaster.scene

import com.blaster.common.euler3
import com.blaster.common.quat
import com.blaster.common.vec3

// the higher the level of command, the higher is the priority:
// target >> euler >> quat >> matrix

data class Marker(
        val uid: String,
        val pos: vec3,
        val euler: euler3? = null, val quat: quat? = null,
        val scale: vec3? = null,
        val scaleTo: vec3? = null,
        val dir: vec3? = null,
        val target: String? = null,
        val custom: String? = null,
        val children: MutableList<Marker> = mutableListOf())