package com.blaster.scene

import com.blaster.common.*

class Node<T>(
        private var parent: Node<T>? = null,
        val position: vec3 = vec3(),
        val rotation: quat = quat().identity(),
        val scale: vec3 = vec3(1f),
        val payload: T? = null) {

    private val children = mutableListOf<Node<T>>()

    private val version = Version()
    private val localM = mat4()
    private val modelM = mat4()

    fun attach(child: Node<T>) {
        if (!children.contains(child)) {
            children.add(child)
            child.parent = this
            child.version.increment()
        }
    }

    private fun detach(child: Node<T>) {
        (children as ArrayList).remove(child)
        child.parent = null
        child.version.increment()
    }

    fun detachFromParent() {
        parent?.detach(this)
    }

    private fun calculateLocalM(): mat4 {
        if (version.check()) {
            localM.identity().translationRotateScale(position, rotation, scale)
        }
        return localM
    }

    fun calculateM(): mat4 {
        val p = parent
        if (p == null) {
            modelM.set(calculateLocalM()) // root
        } else {
            p.calculateM().mul(calculateLocalM(), modelM)
        }
        return modelM
    }

    fun setPosition(pos: vec3): Node<T> {
        position.set(pos)
        version.increment()
        return this
    }

    fun setRotation(rot: quat): Node<T> {
        rotation.set(rot)
        version.increment()
        return this
    }

    fun setScale(value: vec3): Node<T> {
        scale.set(value)
        version.increment()
        return this
    }

    fun setScale(value: Float): Node<T> {
        scale.set(value)
        version.increment()
        return this
    }

    fun setEulerDeg(degrees: vec3): Node<T> {
        rotation.identity().rotateXYZ(radf(degrees.x), radf(degrees.y), radf(degrees.z))
        version.increment()
        return this
    }

    fun rotate(axis: vec3, angle: Float): Node<T> {
        rotation.rotateAxis(angle, axis)
        version.increment()
        return this
    }

    fun lookAlong(direction: vec3): Node<T> {
        rotation.lookAlong(direction, VECTOR_UP)
        version.increment()
        return this
    }

    fun lookAt(target: vec3): Node<T> {
        val direction = vec3(target).sub(position)
        rotation.lookAlong(direction, VECTOR_UP)
        version.increment()
        return this
    }

    fun setDefaultPosition() {
        position.zero()
        version.increment()
    }

    fun setDefaultRotation() {
        rotation.identity()
        version.increment()
    }

    fun setDefaultScale() {
        scale.set(1f)
        version.increment()
    }
}