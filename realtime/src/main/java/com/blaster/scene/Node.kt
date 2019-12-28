package com.blaster.scene

import org.joml.Matrix4f
import org.joml.Vector3f

// todo add versions or just straight compare
class Node(private val parent: Node? = null) {
    private val modelM = Matrix4f()

    fun calculateViewM(): Matrix4f =
            if (parent == null) {
                modelM
            } else {
                val result = Matrix4f()
                parent.calculateViewM().mul(modelM, result)
                result
            }

    fun tick() {
        modelM.rotate(0.01f, Vector3f(0f, 1f, 0f))
    }
}