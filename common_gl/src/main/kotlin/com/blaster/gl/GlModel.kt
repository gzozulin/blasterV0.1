package com.blaster.gl

import com.blaster.common.AABB
import com.blaster.scene.Node

class GlModel (val mesh: GlMesh, val diffuse: GlTexture, val aabb: AABB = AABB()) : Node()