package com.gzozulin.wallpaper.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import com.gzozulin.wallpaper.gl.GLTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TextureLib (private val ctx: Context) {
    fun loadTexture(filename: String, unit: Int = 0): GLTexture {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val decoded = BitmapFactory.decodeStream(ctx.assets.open(filename), null, options)
        val buffer = ByteBuffer.allocateDirect(decoded!!.byteCount).order(ByteOrder.nativeOrder())
        decoded.copyPixelsToBuffer(buffer)
        buffer.position(0)
        val tex = GLTexture(
                unit = unit,
                width = decoded.width, height = decoded.height,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_UNSIGNED_BYTE,
                pixels = buffer)
        decoded.recycle()
        return tex
    }
}