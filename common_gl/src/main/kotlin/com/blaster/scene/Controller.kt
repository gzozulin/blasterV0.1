package com.blaster.scene

import com.blaster.common.VECTOR_UP
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

private const val VELOCITY = 0.01f

class Controller(private val sensitivity: Float = 0.005f) {
    var w = false
    var a = false
    var s = false
    var d = false

    val position = Vector3f()

    private var yaw = Math.toRadians(-90.0).toFloat()
    private var pitch = 0f
    private var roll = 0f

    val direction = Vector3f(0f, 0f, -1f)

    fun yaw(radians: Float) {
        yaw += (radians * sensitivity)
    }

    fun pitch(radians: Float) {
        pitch += (radians * sensitivity)
    }

    fun roll(radians: Float) {
        roll += (radians * sensitivity)
    }

    private val delta = Vector3f()
    private val temp = Vector3f()
    private fun updatePosition() {
        delta.zero()
        temp.zero()
        if (w) {
            delta.add(direction)
        }
        if (a) {
            VECTOR_UP.cross(direction, temp)
            temp.normalize()
            delta.add(temp)
        }
        if (s) {
            direction.negate(temp)
            delta.add(temp)
        }
        if (d) {
            VECTOR_UP.cross(direction, temp)
            temp.normalize().negate()
            delta.add(temp)
        }
        delta.mul(VELOCITY)
        position.add(delta)
    }

    private fun updateDirection() {
        direction.x = cos(yaw) * cos(pitch)
        direction.y = sin(pitch)
        direction.z = sin(yaw) * cos(pitch)
    }

    fun apply(apply: (position: Vector3f, direction: Vector3f) -> Unit) {
        updatePosition()
        updateDirection()
        apply.invoke(position, direction)
    }
}