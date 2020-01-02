package com.blaster.scene

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(aspectRatio: Float) : Node() {
    val projectionM = Matrix4f()

    init {
        projectionM.perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 1f, 4000f)
    }

    private val negatedPosition = Vector3f()

    override fun calculateLocalM(): Matrix4f {
        if (localMatrixVersion != localMatrixLastVersion) {
            internalPosition.negate(negatedPosition)
            localM.identity().rotate(internalRotation).translate(negatedPosition)
            localMatrixLastVersion = localMatrixVersion
        }
        return localM
    }
}