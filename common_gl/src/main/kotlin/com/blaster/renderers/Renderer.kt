package com.blaster.renderers

interface Renderer {
    fun onCreate()
    fun onChange(width: Int, height: Int)
    fun onDraw()
}