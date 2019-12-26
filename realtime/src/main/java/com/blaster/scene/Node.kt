package com.blaster.scene

// todo add versions or just straight compare
class Node(private val parent: Node? = null) {
    private val modelM = Mat4()

    fun calculateViewM(): Mat4 =
            if (parent == null) { modelM } else { parent.calculateViewM() * modelM }

    fun tick() {
        modelM.rotate(1f, Vec3(0f, 1f, 0f))
    }
}