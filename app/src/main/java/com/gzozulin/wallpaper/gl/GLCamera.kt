package com.gzozulin.wallpaper.gl

class GLCamera(aspectRatio: Float) {
    val viewM = Matrix4f()
    val projectionM = Matrix4f()

    init {
        projectionM.frustumInplace(-aspectRatio, aspectRatio, -1f, 1f, 1f, 5f)
    }

    fun lookAt(from: Vector3f, to: Vector3f) {
        viewM.lookAtInplace(from, to, Vector3f(y = 1f))
    }
}

class GLNode(private val parent: GLNode? = null) {
    private val modelM = Matrix4f()

    fun calculateViewM(): Matrix4f =
            if (parent == null) { modelM } else { modelM * parent.calculateViewM() }

    fun once() {
        modelM.translateInplace(Vector3f(x = 1f))
        modelM.scaleInplace(Vector3f(0.7f))
    }

    fun tick() {
        modelM.rotateInplace(1f, Vector3f(0f, 1f, 0f))
    }
}