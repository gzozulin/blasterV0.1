package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import com.blaster.renderers.SimpleRenderer

private val simpleRenderer = SimpleRenderer()

private val particles = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        simpleRenderer.onCreate()
        simpleRenderer.onChange(width, height)
    }

    override fun onDraw() {
        simpleRenderer.onDraw()
    }
}

fun main() {
    particles.show()
}