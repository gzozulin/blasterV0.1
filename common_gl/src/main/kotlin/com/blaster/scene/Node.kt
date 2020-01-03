package com.blaster.scene

import com.blaster.math.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Node : Movable {
    private var parent: Node? = null
    val children: List<Node> = ArrayList()

    private val internalPosition: Vector3f = Vector3f()
    private val internalRotation: Quaternionf = Quaternionf()
    private val internalScale: Vector3f = Vector3f(1f)

    private val positionBuf = Vector3f()
    val position: Vector3f
        get() {
            calculateModelM().getTranslation(positionBuf)
            return positionBuf
        }

    private var localMatrixVersion = 0
    private var localMatrixLastVersion = Int.MAX_VALUE
    private val localM = Matrix4f()

    private val modelM = Matrix4f()

    var graphVersion = 0
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
        if (localMatrixVersion != localMatrixLastVersion) {
            localM.identity().rotate(internalRotation).scale(internalScale).translate(internalPosition)
            localMatrixLastVersion = localMatrixVersion
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
        localMatrixLastVersion++
        internalRotation.rotateAxis(0.01f, VECTOR_UP)
    }
}