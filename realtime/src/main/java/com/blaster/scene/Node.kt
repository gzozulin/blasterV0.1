package com.blaster.scene

import com.blaster.math.Mat4
import com.blaster.math.Vec3

// todo add versions or just straight compare
class Node(private val parent: Node? = null) {
    private val modelM = com.blaster.math.Mat4()

    fun calculateViewM(): com.blaster.math.Mat4 =
            if (parent == null) { modelM } else { parent.calculateViewM() * modelM }

    fun tick() {
        modelM.rotate(1f, Vec3(0f, 1f, 0f))
    }
}