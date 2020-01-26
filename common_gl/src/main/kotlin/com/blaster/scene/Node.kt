package com.blaster.scene

import com.blaster.common.*

interface Payload {
    val aabb: aabb
}

class Node<T : Payload>(
        private var parent: Node<T>? = null,
        private val position: vec3 = vec3(),
        private val rotation: quat = quat(),
        private val scale: vec3 = vec3(1f),
        val payload: T? = null) {

    private val children: List<Node<T>> = ArrayList()
    private var graphVersion = Version()

    private val localVersion = Version()
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

    private fun calculateLocalM(): mat4 {
        if (localVersion.check()) {
            localM.identity().scale(scale).translate(position).rotate(rotation)
        }
        return localM
    }

    fun calculateM(): mat4 {
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

    fun setRotation(rot: quat): Node<T> {
        rotation.set(rot)
        localVersion.increment()
        return this
    }

    fun setScale(value: vec3): Node<T> {
        scale.set(value)
        localVersion.increment()
        return this
    }

    fun setEulerDeg(degrees: vec3): Node<T> {
        rotation.identity().rotateXYZ(radf(degrees.x), radf(degrees.y), radf(degrees.z))
        localVersion.increment()
        return this
    }

    fun rotate(axis: vec3, angle: Float): Node<T> {
        rotation.rotateAxis(angle, axis)
        localVersion.increment()
        return this
    }

    fun lookAlong(direction: vec3): Node<T> {
        rotation.lookAlong(direction, VECTOR_UP)
        localVersion.increment()
        return this
    }

    fun lookAt(target: vec3): Node<T> {
        val direction = vec3()
        target.sub(position, direction)
        rotation.lookAlong(direction, VECTOR_UP)
        localVersion.increment()
        return this
    }
}