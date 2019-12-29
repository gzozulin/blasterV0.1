package com.blaster.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.blaster.assets.*
import com.blaster.renderers.DeferredRenderer
import com.blaster.renderers.SimpleRenderer
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    private val assetStream = object : AssetStream {
        override fun openAsset(filename: String) =
                Thread.currentThread().contextClassLoader.getResource(filename)!!.openStream()
    }

    private val pixelDecoder = object : PixelDecoder {
        override fun decodePixels(inputStream: InputStream): PixelDecoder.Decoded {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val decoded = BitmapFactory.decodeStream(inputStream, null, options)
            val buffer = ByteBuffer.allocateDirect(decoded!!.byteCount).order(ByteOrder.nativeOrder())
            decoded.copyPixelsToBuffer(buffer)
            buffer.position(0)
            return PixelDecoder.Decoded(buffer, decoded.width, decoded.height)
                    .also { decoded.recycle() }
        }
    }

    private val shaderLib = ShadersLib(assetStream)
    private val textureLib = TexturesLib(assetStream, pixelDecoder)
    private val modelsLib = ModelsLib(assetStream, textureLib)

    private val simple = object : Renderer {
        private val renderer = SimpleRenderer(shaderLib, textureLib)

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
        private val renderer = DeferredRenderer(shaderLib, modelsLib)

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