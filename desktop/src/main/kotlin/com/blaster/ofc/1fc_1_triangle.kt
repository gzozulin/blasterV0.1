package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import com.blaster.renderers.SimpleRenderer

private const val WIDTH = 800
private const val HEIGHT = 600

private val renderer = SimpleRenderer()

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate() {
        renderer.onCreate()
        renderer.onChange(WIDTH, HEIGHT)
    }

    override fun onDraw() {
        renderer.onDraw()
    }
}

fun main() {
    window.show()
}