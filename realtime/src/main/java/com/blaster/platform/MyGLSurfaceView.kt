package com.blaster.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import assets.*
import com.blaster.renderers.DeferredRenderer
import com.blaster.renderers.SimpleRenderer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    private val simple = object : Renderer {
        private val renderer = SimpleRenderer()

        override fun onDrawFrame(gl: GL10?) {
            renderer.onDraw()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            renderer.onChange(width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            renderer.onCreate()
        }
    }

    private val deferred = object : Renderer {
        private val renderer = DeferredRenderer()

        override fun onDrawFrame(gl: GL10?) {
            renderer.onDraw()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            renderer.onChange(width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            renderer.onCreate()
        }
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(simple)
        //setRenderer(deferred)
    }
}