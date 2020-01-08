package com.blaster.scene

import com.blaster.common.AABB
import com.blaster.gl.GlMesh
import com.blaster.gl.GlTexture

data class Model (val mesh: GlMesh, val diffuse: GlTexture, val aabb: AABB = AABB()) : Node()