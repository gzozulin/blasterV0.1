package com.blaster.scene

import com.blaster.common.Version
import com.blaster.common.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Node<T>(
        private var parent: Node<T>? = null,
        private val position: Vector3f = Vector3f(),
        private val rotation: Quaternionf = Quaternionf(),
        private val scale: Vector3f = Vector3f(1f),
        var payload: T? = null) {

    private val children: List<Node<T>> = ArrayList()
    var graphVersion = Version()

    val localVersion = Version()
    private val localM = Matrix4f()
    private val modelM = Matrix4f()

    private val absolutePositionBuf = Vector3f()
    val absolutePosition: Vector3f
        get() {
            calculateModelM().getTranslation(absolutePositionBuf)
            return absolutePositionBuf
        }

    private val absoluteRotationBuf = Quaternionf()
    val absoluteRotation: Quaternionf
        get() {
            calculateModelM().getNormalizedRotation(absoluteRotationBuf)
            return absoluteRotationBuf
        }

    private fun incrementVersion() {
        graphVersion.increment()
        if (parent != null) {
            parent!!.graphVersion.increment()
        }
    }

    fun attach(child: Node<T>) {
        if (!children.contains(child)) {
            (children as ArrayList).add(child)
            child.parent = this
            incrementVersion()
        }
    }

    fun detach(child: Node<T>) {
        (children as ArrayList).remove(child)
        child.parent = null
        incrementVersion()
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

    // todo: remove
    fun tick() {
        localVersion.increment()
        rotation.rotateAxis(0.01f, VECTOR_UP)
    }
}