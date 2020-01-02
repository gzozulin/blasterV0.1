package com.blaster.scene

import com.blaster.math.AABB
import com.blaster.math.VECTOR_UP
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

open class Node {
    private var parent: Node? = null
    val children: List<Node> = ArrayList()

    protected val internalPosition: Vector3f = Vector3f()
    protected val internalRotation: Quaternionf = Quaternionf()
    private val internalScale: Vector3f = Vector3f(1f)

    val position
        get() = internalPosition // todo: should be absolute from matrix

    protected var localMatrixVersion = 0
    protected var localMatrixLastVersion = Int.MAX_VALUE
    protected val localM = Matrix4f()

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

    fun calculateTransformM(): Matrix4f {
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

    private val direction = Vector3f()
    fun lookAt(from: Vector3f, to: Vector3f) {
        localMatrixLastVersion++
        internalPosition.set(from)
        to.sub(from, direction).normalize()
        internalRotation.lookAlong(direction, VECTOR_UP)
    }

    fun lookAt(aabb: AABB) {
        localMatrixLastVersion++
        var maxValue = aabb.width
        if (aabb.height > maxValue) {
            maxValue = aabb.height
        }
        if (aabb.depth > maxValue) {
            maxValue = aabb.depth
        }
        val center = aabb.center
        center.add(Vector3f(0f, maxValue / 2f, maxValue), internalPosition)
        lookAt(internalPosition, center)
    }
}