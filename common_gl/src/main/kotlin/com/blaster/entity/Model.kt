package com.blaster.entity

import com.blaster.gl.GlTexture
import com.blaster.scene.Mesh
import com.blaster.scene.Payload
import org.joml.AABBf

data class Model (val mesh: Mesh, val diffuse: GlTexture, override val aabb: AABBf = AABBf()) : Payload