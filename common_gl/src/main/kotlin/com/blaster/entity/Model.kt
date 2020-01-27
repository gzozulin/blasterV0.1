package com.blaster.entity

import com.blaster.common.aabb
import com.blaster.gl.GlTexture
import com.blaster.scene.Mesh
import com.blaster.scene.Payload

data class Model (
        val mesh: Mesh, val diffuse: GlTexture,
        override val aabb: aabb = aabb(),
        val material: Material = Material.DUMMY) : Payload