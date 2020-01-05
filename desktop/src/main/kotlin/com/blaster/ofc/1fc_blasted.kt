package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

// WYSIWYG scene editor

val json = """
    {
        mesh = "teapot.obj"
        diffuse = "teapot_diffuse.png"
        position = 123
        scale = 123
        rotation = 123
        
        children = {
            light
            color = 123
            position = 2123
            rotation
            etc...
        }
"""


private val window = object : LwjglWindow(800, 600) {
    override fun onCreate() {

    }

    override fun onDraw() {

    }
}

fun main() {
    //window.show()
}
