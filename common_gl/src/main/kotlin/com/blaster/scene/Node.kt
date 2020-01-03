package com.blaster.scene

import com.blaster.math.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Node : Movable {
    private var parent: Node? = null
    val children: List<Node> = ArrayList()
    var graphVersion = 0

    private val internalPosition: Vector3f = Vector3f()
    private val internalRotation: Quaternionf = Quaternionf()
    private val internalScale: Vector3f = Vector3f(1f)

    private var localVersion = 0
    private var localLastVersion = Int.MAX_VALUE
    private val localM = Matrix4f()
    private val modelM = Matrix4f()

    private val positionBuf = Vector3f()
    val absolutePosition: Vector3f
        get() {
            calculateModelM().getTranslation(positionBuf)
            return positionBuf
        }

    private fun incrementVersion() {
        graphVersion++
        if (parent != null) {
            parent!!.graphVersion++
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
        if (localVersion != localLastVersion) {
            localM.identity().rotate(internalRotation).scale(internalScale).translate(internalPosition)
            localLastVersion = localVersion
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
        localLastVersion++
        internalRotation.rotateAxis(0.01f, VECTOR_UP)
    }
}