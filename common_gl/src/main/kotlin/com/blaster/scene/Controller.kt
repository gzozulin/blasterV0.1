package com.blaster.scene

import com.blaster.common.VECTOR_UP
import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

// todo: some inertia would be cool
// todo: can directly implement lwjgl listeners for keyb and cursor
class Controller(private val sensitivity: Float = 0.005f, private val velocity: Float = 0.01f) {
    var w = false
    var a = false
    var s = false
    var d = false
    var q = false
    var e = false

    // todo: 0-9 - teleports
    // 0 - starting point

    val position = Vector3f()

    private var yaw = Math.toRadians(-90.0).toFloat()
    private var pitch = 0f
    private var roll = 0f

    val forward = Vector3f(0f, 0f, -1f)

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
    private val back = Vector3f()
    private val right = Vector3f()
    private val left = Vector3f()
    private val up = Vector3f()
    private val down = Vector3f()
    private fun updatePosition() {
        delta.zero()
        VECTOR_UP.cross(forward, right)
        right.normalize()
        forward.negate(back)
        right.negate(left)
        forward.cross(right, up)
        up.normalize()
        up.negate(down)
        if (w) {
            delta.add(forward)
        }
        if (a) {
            delta.add(right)
        }
        if (s) {
            delta.add(back)
        }
        if (d) {
            delta.add(left)
        }
        if (q) {
            delta.add(down)
        }
        if (e) {
            delta.add(up)
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