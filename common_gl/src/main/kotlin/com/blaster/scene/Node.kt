package com.blaster.scene

import com.blaster.common.*
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

interface Payload {
    val aabb: aabb
}

class Node<T : Payload>(
        private var parent: Node<T>? = null,
        private val position: vec3 = vec3(),
        private val rotation: quat = quat(),
        private val scale: vec3 = vec3(1f),
        var payload: T? = null) {

    private val children: List<Node<T>> = ArrayList()
    var graphVersion = Version()

    val localVersion = Version()
    private val localM = mat4()
    private val modelM = mat4()

    private fun incrementGraph() {
        graphVersion.increment()
        if (parent != null) {
            parent!!.graphVersion.increment()
        }
    }

    fun attach(child: Node<T>) {
        if (!children.contains(child)) {
            (children as ArrayList).add(child)
            child.parent = this
            incrementGraph()
        }
    }

    fun detach(child: Node<T>) {
        (children as ArrayList).remove(child)
        child.parent = null
        incrementGraph()
    }

    private fun calculateLocalM(): Matrix4f {
        if (localVersion.check()) {
            localM.identity().rotate(rotation).scale(scale).translate(position)
        }
        return localM
    }

    fun calculateModelM(): Matrix4f {
        if (parent == null) {
            return calculateLocalM() // root
        }
        calculateLocalM().mul(parent!!.calculateLocalM(), modelM)
        return modelM
    }

    fun setPosition(pos: vec3): Node<T> {
        position.set(pos)
        localVersion.increment()
        return this
    }

    fun setEuler(euler: vec3): Node<T> {
        rotation.identity().rotateXYZ(euler.x, euler.y, euler.z)
        localVersion.increment()
        return this
    }

    fun setScale(value: vec3): Node<T> {
        scale.set(value)
        localVersion.increment()
        return this
    }

    fun rotate(axis: vec3, angle: Float) {
        rotation.rotateAxis(angle, axis)
        localVersion.increment()
    }
}