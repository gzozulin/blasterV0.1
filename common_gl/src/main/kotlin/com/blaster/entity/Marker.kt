package com.blaster.entity

import com.blaster.common.*
import com.blaster.scene.Controller
import com.blaster.scene.Node
import com.blaster.scene.Payload

// the higher the level of command, the higher is the priority:
// target >> dir >> euler >> quat >> matrix

data class Marker(
        val uid: String,
        val pos: vec3,
        val euler: euler3? = null, val quat: quat? = null,
        val scale: vec3? = null,
        val bound: Float? = null,
        val dir: vec3? = null,
        val target: vec3? = null,
        val custom: String? = null,
        val children: MutableList<Marker> = mutableListOf()) {

    fun <T : Payload> apply(node: Node<T>) {
        node.setPosition(pos)
        when {
            target != null -> node.lookAt(target)
            dir != null -> node.lookAlong(dir)
            euler != null -> node.setEulerDeg(euler)
            quat != null -> node.setRotation(quat)
        }
        if (bound != null) {
            val factor = node.payload!!.aabb.scaleTo(bound)
            node.setScale(vec3(factor))
        } else if (scale != null) {
            node.setScale(scale)
        }
    }

    fun apply(controller: Controller) {
        controller.position.set(pos)
        if (euler != null) {
            controller.pitch = radf(euler.x)
            controller.yaw = radf(euler.y)
            controller.roll = radf(euler.z)
        }
    }
}

