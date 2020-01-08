package com.blaster.ofc

import com.blaster.gl.GlBuffer
import com.blaster.platform.LwjglWindow

// simple particles - all mathematics on cpu, only instancing and points on gpu

private const val W = 800
private const val H = 600

class ParticlesTechnique {
    private lateinit var buffer: GlBuffer

    fun prepare() {

    }

    fun draw() {

    }
}

private val technique = ParticlesTechnique()

private val window = object : LwjglWindow(W, H) {
    override fun onCreate() {
        technique.prepare()
    }

    override fun onDraw() {
        technique.draw()
    }
}

fun main() {
    window.show()
}