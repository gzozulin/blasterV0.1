package com.blaster.platform

import com.blaster.entity.Controller
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

class WasdInput(private val controller: Controller) {

    fun onCursorDelta(delta: Vector2f) {
        controller.yaw(delta.x)
        controller.pitch(-delta.y)
    }

    fun keyPressed(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.moveForward = true
            GLFW.GLFW_KEY_A -> controller.moveLeft = true
            GLFW.GLFW_KEY_S -> controller.moveBack = true
            GLFW.GLFW_KEY_D -> controller.moveRight = true
            GLFW.GLFW_KEY_E -> controller.moveUp = true
            GLFW.GLFW_KEY_Q -> controller.moveDown = true
        }
    }

    fun keyReleased(key: Int) {
        when (key) {
            GLFW.GLFW_KEY_W -> controller.moveForward = false
            GLFW.GLFW_KEY_A -> controller.moveLeft = false
            GLFW.GLFW_KEY_S -> controller.moveBack = false
            GLFW.GLFW_KEY_D -> controller.moveRight = false
            GLFW.GLFW_KEY_E -> controller.moveUp = false
            GLFW.GLFW_KEY_Q -> controller.moveDown = false
        }
    }
}