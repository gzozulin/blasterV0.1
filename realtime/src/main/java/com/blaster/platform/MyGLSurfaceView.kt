package com.blaster.platform

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.blaster.renderers.DeferredRenderer
import com.blaster.renderers.SimpleRenderer

class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        setEGLContextClientVersion(2)
        //val renderer = SimpleRenderer(context!!)
        val renderer = DeferredRenderer(context!!)
        setRenderer(renderer)
    }
}