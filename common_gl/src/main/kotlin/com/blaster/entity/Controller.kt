package com.blaster.entity

import com.blaster.common.VECTOR_DOWN
import com.blaster.common.VECTOR_UP
import com.blaster.common.vec3
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

private const val maxPitch = Math.PI.toFloat() / 2f - 0.1f
private const val minPitch = -Math.PI.toFloat() / 2f + 0.1f

// todo: 0-9 - teleports
// todo: 0 - starting point
// todo: separate controller moder and create class for input from kb and mouse
// todo: some inertia would be cool
// todo: can directly implement lwjgl listeners for keyb and cursor
data class Controller(
        val position: vec3 = vec3(),
        var yaw: Float = Math.toRadians(-90.0).toFloat(),
        var pitch: Float = 0f,
        var roll: Float = 0f,
        private val sensitivity: Float = 0.005f,
        private val velocity: Float = 0.01f
) {
    var moveForward: Boolean     = false
    var moveLeft: Boolean        = false
    var moveBack: Boolean        = false
    var moveRight: Boolean       = false
    var moveDown: Boolean        = false
    var moveUp: Boolean          = false

    private val delta: vec3 = Vector3f()
    private val back: vec3 = Vector3f()
    private val right: vec3 = Vector3f()
    private val left: vec3 = Vector3f()

    private val forward = Vector3f(0f, 0f, -1f)

    fun yaw(radians: Float) {
        yaw += (radians * sensitivity)
    }

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