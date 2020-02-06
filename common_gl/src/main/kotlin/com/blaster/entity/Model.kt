package com.blaster.entity

import com.blaster.common.aabb
import com.blaster.gl.GlTexture
import com.blaster.tools.Mesh

data class Model (
        val mesh: Mesh, val diffuse: GlTexture,
        val aabb: aabb = aabb(),
        val material: Material = Material.CONCRETE)