package com.blaster.gl

import com.blaster.math.AABB
import com.blaster.scene.Node

class GLModel (val mesh: GLMesh, val diffuse: GLTexture, val aabb: AABB) : Node()