package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import com.blaster.renderers.SimpleRenderer

private val renderer = SimpleRenderer()

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