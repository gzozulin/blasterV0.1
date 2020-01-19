package com.blaster.scene

import com.blaster.gl.GlTexture
import org.joml.AABBf

data class Model (val mesh: Mesh, val diffuse: GlTexture, val aabb: AABBf = AABBf())