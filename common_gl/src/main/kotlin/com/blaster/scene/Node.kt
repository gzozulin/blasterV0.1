package com.blaster.scene

import com.blaster.common.Version
import com.blaster.common.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Node(
        private var parent: Node? = null,
        private val relativePosition: Vector3f = Vector3f(),
        private val relativeRotation: Quaternionf = Quaternionf(),
        private val relativeScale: Vector3f = Vector3f(1f)) {

    private val children: List<Node> = ArrayList()
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

    private val absoluteRotationBuf = Vector3f()
    val absoluteRotation: Vector3f
        get() {
            TODO()
        }

    private fun incrementVersion() {
        graphVersion.increment()
        if (parent != null) {
            parent!!.graphVersion.increment()
        }
    }

    fun attach(child: Node) {
        if (!children.contains(child)) {
            (children as ArrayList).add(child)
            child.parent = this
            incrementVersion()
        }
    }

    fun detach(child: Node) {
        (children as ArrayList).remove(child)
        child.parent = null
        incrementVersion()
    }

    private fun calculateLocalM(): Matrix4f {
        if (localVersion.check()) {
            localM.identity().rotate(relativeRotation).scale(relativeScale).translate(relativePosition)
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

    fun tick() {
        localVersion.increment()
        relativeRotation.rotateAxis(0.01f, VECTOR_UP)
    }
}