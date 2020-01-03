package com.blaster.scene

import com.blaster.common.Version
import com.blaster.common.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Node : Movable {
    private var parent: Node? = null
    val children: List<Node> = ArrayList()
    var graphVersion = Version()

    val position: Vector3f = Vector3f()
    val rotation: Quaternionf = Quaternionf()
    val scale: Vector3f = Vector3f(1f)

    val localVersion = Version()
    private val localM = Matrix4f()
    private val modelM = Matrix4f()

    private val absolutePositionBuf = Vector3f()
    val absolutePosition: Vector3f
        get() {
            calculateModelM().getTranslation(absolutePositionBuf)
            return absolutePositionBuf
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

    protected open fun calculateLocalM(): Matrix4f {
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

    fun tick() {
        localVersion.increment()
        rotation.rotateAxis(0.01f, VECTOR_UP)
    }
}