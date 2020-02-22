package com.blaster.entity

import com.blaster.auxiliary.aabb
import com.blaster.gl.GlTexture
import com.blaster.gl.GlMesh

data class Model (
        val mesh: GlMesh, val diffuse: GlTexture,
        val aabb: aabb = aabb(),
        val material: Material = Material.CONCRETE)