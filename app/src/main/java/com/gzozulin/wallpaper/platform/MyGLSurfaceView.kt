package com.gzozulin.wallpaper.platform

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.gzozulin.wallpaper.renderers.DeferredMultipassRenderer

class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        setEGLContextClientVersion(2)
        //val renderer = SimpleRenderer(context!!)
        val renderer = DeferredMultipassRenderer(context!!)
        setRenderer(renderer)
    }
}