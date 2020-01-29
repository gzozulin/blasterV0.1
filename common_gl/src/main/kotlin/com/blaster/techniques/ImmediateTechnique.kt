package com.blaster.techniques

import com.blaster.common.mat4
import com.blaster.common.vec3
import com.blaster.gl.GlLocator
import com.blaster.gl.glCheck
import com.blaster.scene.Camera
import org.joml.AABBf
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

class ImmediateTechnique {
    private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())

    fun prepare(camera: Camera) {
        glCheck {
            backend.glMatrixMode(backend.GL_PROJECTION)
            camera.projectionM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
        }
    }

    fun aabb(camera: Camera, aabb: AABBf, modelM: mat4, color: Vector3f = Vector3f(1f)) {
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            val modelViewM = mat4(camera.calculateViewM()).mul(modelM)
            modelViewM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            backend.glColor3f(color.x, color.y, color.z)
            val bottomLeftBack = vec3(aabb.minX, aabb.minY, aabb.minZ)
            val bottomLeftFront = vec3(aabb.minX, aabb.minY, aabb.maxZ)
            val bottomRightBack = vec3(aabb.maxX, aabb.minY, aabb.minZ)
            val bottomRightFront = vec3(aabb.maxX, aabb.minY, aabb.maxZ)
            val topLeftBack = vec3(aabb.minX, aabb.maxY, aabb.minZ)
            val topLeftFront = vec3(aabb.minX, aabb.maxY, aabb.maxZ)
            val topRightBack = vec3(aabb.maxX, aabb.maxY, aabb.minZ)
            val topRightFront = vec3(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glVertex3f(bottomLeftBack)
            backend.glVertex3f(bottomLeftFront)
            backend.glVertex3f(bottomLeftFront)
            backend.glVertex3f(bottomRightFront)
            backend.glVertex3f(bottomRightFront)
            backend.glVertex3f(bottomRightBack)
            backend.glVertex3f(bottomRightBack)
            backend.glVertex3f(bottomLeftBack)
            backend.glVertex3f(topLeftBack)
            backend.glVertex3f(topLeftFront)
            backend.glVertex3f(topLeftFront)
            backend.glVertex3f(topRightFront)
            backend.glVertex3f(topRightFront)
            backend.glVertex3f(topRightBack)
            backend.glVertex3f(topRightBack)
            backend.glVertex3f(topLeftBack)
            backend.glVertex3f(bottomLeftBack)
            backend.glVertex3f(topLeftBack)
            backend.glVertex3f(bottomLeftFront)
            backend.glVertex3f(topLeftFront)
            backend.glVertex3f(bottomRightBack)
            backend.glVertex3f(topRightBack)
            backend.glVertex3f(bottomRightFront)
            backend.glVertex3f(topRightFront)
            backend.glEnd()
        }
    }

    // todo: draw light
    // todo: draw axis
}