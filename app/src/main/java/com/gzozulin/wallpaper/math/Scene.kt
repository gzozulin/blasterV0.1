package com.gzozulin.wallpaper.math

class SceneCamera(aspectRatio: Float) {
    val viewM = Matrix4f()
    val projectionM = Matrix4f()

    var eye = Vector3f()
    var center = Vector3f()

    init {
        projectionM.perspectiveInplace(90f, aspectRatio, 1f, 4000f)
    }

    fun lookAt(from: Vector3f, to: Vector3f) {
        viewM.lookAtInplace(from, to, Vector3f(y = 1f))
        eye = from
        center = to
    }

    fun lookAt(aabb: AABB) {
        var maxValue = aabb.width
        if (aabb.height > maxValue) {
            maxValue = aabb.height
        }
        if (aabb.depth > maxValue) {
            maxValue = aabb.depth
        }
        val center = aabb.center
        val from = center + Vector3f(y = maxValue / 2f, z = maxValue)
        lookAt(from, center)
    }
}

// todo add versions or just straight compare
class SceneNode(private val parent: SceneNode? = null) {
    private val modelM = Matrix4f()

    fun calculateViewM(): Matrix4f =
            if (parent == null) { modelM } else { parent.calculateViewM() * modelM }

    fun tick() {
        modelM.rotateInplace(1f, Vector3f(0f, 1f, 0f))
    }
}