package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import com.blaster.renderers.DeferredRenderer

// todo templates -> setup on the scene

// template model
// template light
// template billboard
// template ps
// etc

private val renderer = DeferredRenderer()

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        renderer.onCreate()
        renderer.onChange(width, height)
    }

    override fun onDraw() {
        renderer.onDraw()
    }
}

fun main() {
    window.show()
}