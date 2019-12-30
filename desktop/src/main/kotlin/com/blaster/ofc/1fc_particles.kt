package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import com.blaster.renderers.SimpleRenderer

// todo: store the current position of particle in the texture object (100x100 tex == 10000 particles)
// todo: gravity affected particles, ground plane
// http://www.opengl-tutorial.org/intermediate-tutorials/billboards-particles/particles-instancing/

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