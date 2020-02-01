package com.blaster.scene

import com.blaster.common.VECTOR_DOWN
import com.blaster.common.VECTOR_UP
import com.blaster.common.vec3
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

// todo: separate controller moder and create class for input from kb and mouse
// todo: some inertia would be cool
// todo: can directly implement lwjgl listeners for keyb and cursor
class Controller(
        val position: vec3 = vec3(),
        private val sensitivity: Float = 0.005f, private val velocity: Float = 0.01f
) {
    var moveForward     = false
    var moveLeft        = false
    var moveBack        = false
    var moveRight       = false
    var moveDown        = false
    var moveUp          = false

    // todo: 0-9 - teleports
    // 0 - starting point

    var yaw = Math.toRadians(-90.0).toFloat()
    var pitch = 0f
    var roll = 0f

    val forward = Vector3f(0f, 0f, -1f)

    fun yaw(radians: Float) {
        yaw += (radians * sensitivity)
    }

    private val maxPitch = Math.PI.toFloat() / 2f - 0.1f
    private val minPitch = -Math.PI.toFloat() / 2f + 0.1f
    fun pitch(radians: Float) {
        pitch += (radians * sensitivity)
        if (pitch > maxPitch) {
            pitch = maxPitch
        }
        if (pitch < minPitch) {
            pitch = minPitch
        }
    }

    fun roll(radians: Float) {
        roll += (radians * sensitivity)
    }

    private val delta = Vector3f()
    private val back = Vector3f()
    private val right = Vector3f()
    private val left = Vector3f()
    private fun updatePosition() {
        delta.zero()
        VECTOR_UP.cross(forward, right)
        right.normalize()
        forward.negate(back)
        right.negate(left)
        if (moveForward) {
            delta.add(forward)
        }
        if (moveLeft) {
            delta.add(right)
        }
        if (moveBack) {
            delta.add(back)
        }
        if (moveRight) {
            delta.add(left)
        }
        if (moveDown) {
            delta.add(VECTOR_DOWN)
        }
        if (moveUp) {
            delta.add(VECTOR_UP)
        }
        delta.mul(velocity)
        position.add(delta)
    }

    private fun updateDirection() {
        forward.x = cos(yaw) * cos(pitch)
        forward.y = sin(pitch)
        forward.z = sin(yaw) * cos(pitch)
    }

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
        updatePosition()
        updateDirection()
        apply.invoke(position, forward)
    }
}