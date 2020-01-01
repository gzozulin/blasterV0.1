package com.blaster.scene

import com.blaster.math.VECTOR_UP
import org.joml.Matrix4f

open class Node {
    private var parent: Node? = null
    val children = mutableListOf<Node>()

    private val localM = Matrix4f()
    private val modelM = Matrix4f()

    var version = 0
    private fun incrementVersion() {
        version++
        if (parent != null) {
            parent!!.version++
        }
    }

    fun attach(child: Node) {
        if (!children.contains(child)) {
            children.add(child)
            child.parent = this
            incrementVersion()
        }
    }

    fun detach(child: Node) {
        children.remove(child)
        child.parent = null
        incrementVersion()
    }

    fun calculateViewM(): Matrix4f {
        if (parent == null) {
            return localM // root
        }
        localM.mul(parent!!.localM, modelM)
        return modelM
    }

    fun tick() {
        localM.rotate(0.01f, VECTOR_UP)
    }
}