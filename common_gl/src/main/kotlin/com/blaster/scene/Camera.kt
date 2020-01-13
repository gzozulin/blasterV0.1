package com.blaster.scene

import com.blaster.common.*
import org.joml.AABBf
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Camera(aspectRatio: Float) : Movable {
    val projectionM = Matrix4f().perspective(Math.toRadians(90.0).toFloat(), aspectRatio, 0.1f, 1000f)

    val position: Vector3f = Vector3f()
    val rotation: Quaternionf = Quaternionf()

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
        viewVersion.increment()
        position.set(newPostion)
    }

    fun lookAlong(direction: Vector3f) {
        viewVersion.increment()
        rotation.identity().lookAlong(direction, VECTOR_UP)
    }
}