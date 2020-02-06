package com.blaster.scene

import com.blaster.common.*
import org.joml.AABBf
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

// todo: attach to node
class Camera {
    val projectionM: mat4 = mat4()

    val position: vec3 = vec3()
    val rotation: quat = quat()

    private var viewVersion = Version()
    private val viewM = Matrix4f()

    private val negatedBuf = Vector3f()
    fun calculateViewM(): Matrix4f {
        if (viewVersion.check()) {
            position.negate(negatedBuf)
            viewM.identity().rotate(rotation).translate(negatedBuf)
        }
        return viewM
    }

    fun setPerspective(aspectRatio: Float): Camera {
        projectionM.identity().perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 0.1f, 1000f)
        viewVersion.increment()
        return this
    }

    fun setPerspective(width: Int, height: Int) {
        setPerspective(width.toFloat() / height.toFloat())
    }

    private val directionBuf = Vector3f()
    fun lookAt(from: Vector3f, to: Vector3f): Camera {
        viewVersion.increment()
        position.set(from)
        to.sub(from, directionBuf).normalize()
        rotation.lookAlong(directionBuf, VECTOR_UP)
        return this
    }

    fun lookAt(aabb: AABBf): Camera {
        viewVersion.increment()
        var maxValue = aabb.width()
        if (aabb.height() > maxValue) {
            maxValue = aabb.height()
        }
        if (aabb.depth() > maxValue) {
            maxValue = aabb.depth()
        }
        val center = aabb.center()
        center.add(Vector3f(0f, maxValue / 2f, maxValue), position)
        return lookAt(position, center)
    }

    fun setPosition(newPostion: Vector3f) {
        position.set(newPostion)
        viewVersion.increment()
    }

    fun lookAlong(direction: Vector3f) {
        rotation.identity().lookAlong(direction, VECTOR_UP)
        viewVersion.increment()
    }
}