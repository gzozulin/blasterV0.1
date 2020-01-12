package com.blaster.scene

import com.blaster.common.AABB
import com.blaster.gl.GlTexture

data class Model (val mesh: Mesh, val diffuse: GlTexture, val aabb: AABB = AABB()) : Node()