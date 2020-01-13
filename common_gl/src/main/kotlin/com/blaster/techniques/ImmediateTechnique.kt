package com.blaster.techniques

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

    fun aabb(camera: Camera, aabb: AABBf, color: Vector3f = Vector3f(1f)) {
        glCheck {
            backend.glMatrixMode(backend.GL_MODELVIEW)
            camera.calculateViewM().get(bufferMat4)
            backend.glLoadMatrix(bufferMat4)
            backend.glBegin(backend.GL_LINES)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.maxY)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.maxY)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.minY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.minX, aabb.maxY, aabb.maxZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.minZ)
            backend.glColor3f(color.x, color.y, color.z)
            backend.glVertex3f(aabb.maxX, aabb.minY, aabb.maxZ)
            backend.glEnd()
        }
    }
}