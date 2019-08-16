package com.gzozulin.wallpaper.platform

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.gzozulin.wallpaper.renderers.DeferredRenderer

class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        setEGLContextClientVersion(2)
        //val renderer = SimpleRenderer(context!!)
        val renderer = DeferredRenderer(context!!)
        setRenderer(renderer)
    }
}