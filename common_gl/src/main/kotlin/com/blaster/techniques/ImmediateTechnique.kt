package com.blaster.techniques

import com.blaster.auxiliary.color
import com.blaster.auxiliary.mat4
import com.blaster.auxiliary.vec3
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

    private fun line(from: vec3, to: vec3, color: color) {
        backend.glColor3f(color.x, color.y, color.z)
        backend.glVertex3f(from)
        backend.glVertex3f(to)
    }

    fun aabb(camera: Camera, aabb: AABBf, modelM: mat4, color: vec3 = vec3(1f)) {
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            val modelViewM = mat4(camera.calculateViewM()).mul(modelM)
            modelViewM.get(bufferMat4)
            val bottomLeftBack = vec3(aabb.minX, aabb.minY, aabb.minZ)
            val bottomLeftFront = vec3(aabb.minX, aabb.minY, aabb.maxZ)
            val bottomRightBack = vec3(aabb.maxX, aabb.minY, aabb.minZ)
            val bottomRightFront = vec3(aabb.maxX, aabb.minY, aabb.maxZ)
            val topLeftBack = vec3(aabb.minX, aabb.maxY, aabb.minZ)
            val topLeftFront = vec3(aabb.minX, aabb.maxY, aabb.maxZ)
            val topRightBack = vec3(aabb.maxX, aabb.maxY, aabb.minZ)
            val topRightFront = vec3(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            line(bottomLeftBack, bottomLeftFront, color)
            line(bottomLeftFront, bottomRightFront, color)
            line(bottomRightFront, bottomRightBack, color)
            line(bottomRightBack, bottomLeftBack, color)
            line(topLeftBack, topLeftFront, color)
            line(topLeftFront, topRightFront, color)
            line(topRightFront, topRightBack, color)
            line(topRightBack, topLeftBack, color)
            line(bottomLeftBack, topLeftBack, color)
            line(bottomLeftFront, topLeftFront, color)
            line(bottomRightBack, topRightBack, color)
            line(bottomRightFront, topRightFront, color)
            backend.glEnd()
        }
    }

    fun marker(camera: Camera, modelM: mat4, color1: vec3, color2: vec3, color3: vec3, scale: Float = 1f) {
        val half = scale / 2f
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
            start.x -= half
            end.set(center)
            end.x += half
            line(start, end, color1)
            start.set(center)
            start.y -= half
            end.set(center)
            end.y += half
            line(start, end, color2)
            start.set(center)
            start.z -= half
            end.set(center)
            end.z += half
            line(start, end, color3)
            backend.glEnd()
        }
    }

    fun marker(camera: Camera, modelM: mat4, color: color, scale: Float = 1f) {
        marker(camera, modelM, color, color, color, scale)
    }
}