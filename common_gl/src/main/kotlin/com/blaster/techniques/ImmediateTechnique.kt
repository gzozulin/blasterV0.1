package com.blaster.techniques

import com.blaster.aux.color
import com.blaster.aux.mat4
import com.blaster.aux.vec3
import com.blaster.gl.GlLocator
import com.blaster.gl.glCheck
import com.blaster.entity.Camera
import org.joml.AABBf
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val backend = GlLocator.locate()

class ImmediateTechnique {
    private val bufferMat4 = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder())

    fun resize(camera: Camera) {
        glCheck {
            backend.glMatrixMode(backend.GL_PROJECTION)
            camera.projectionM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
        }
    }

    fun aabb(camera: Camera, aabb: AABBf, modelM: mat4, color: vec3 = vec3(1f)) {
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

    fun marker(camera: Camera, modelM: mat4, color: color, scale: Float = 1f) {
        marker(camera, modelM, color, color, color, scale)
    }

    fun marker(camera: Camera, modelM: mat4, color1: vec3, color2: vec3, color3: vec3, scale: Float = 1f) {
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            val modelViewM = mat4(camera.calculateViewM()).mul(modelM)
            modelViewM.get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            val center = vec3()
            modelM.translation(center)
            val start = vec3()
            val end = vec3()
            start.set(center)
            start.x -= scale / 2f
            end.set(center)
            end.x += scale / 2f
            backend.glColor3f(color1)
            backend.glVertex3f(start)
            backend.glVertex3f(end)
            start.set(center)
            start.y -= scale / 2f
            end.set(center)
            end.y += scale / 2f
            backend.glColor3f(color2)
            backend.glVertex3f(start)
            backend.glVertex3f(end)
            start.set(center)
            start.z -= scale / 2f
            end.set(center)
            end.z += scale / 2f
            backend.glColor3f(color3)
            backend.glVertex3f(start)
            backend.glVertex3f(end)
            backend.glEnd()
        }
    }
}