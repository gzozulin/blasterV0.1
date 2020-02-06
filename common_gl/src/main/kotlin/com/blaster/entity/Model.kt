package com.blaster.entity

import com.blaster.aux.aabb
import com.blaster.gl.GlTexture
import com.blaster.tools.GlMesh

data class Model (
        val mesh: GlMesh, val diffuse: GlTexture,
        val aabb: aabb = aabb(),
        val material: Material = Material.CONCRETE)