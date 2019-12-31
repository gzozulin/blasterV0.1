package com.blaster.assets

import java.awt.image.DataBufferByte
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

open class PixelDecoder {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    open fun decodePixels(inputStream: InputStream): Decoded {
        val bufferedImage = ImageIO.read(inputStream)
        val byteBuffer: ByteBuffer
        when (val dataBuffer = bufferedImage.raster.dataBuffer) {
            is DataBufferByte -> {
                byteBuffer = ByteBuffer.allocateDirect(dataBuffer.data.size)
                var position = 0
                while (position < dataBuffer.data.size) {
                    val b = dataBuffer.data[position++]
                    val g = dataBuffer.data[position++]
                    val r = dataBuffer.data[position++]
                    val a = dataBuffer.data[position++]
                    byteBuffer.put(a)
                    byteBuffer.put(r)
                    byteBuffer.put(g)
                    byteBuffer.put(b)
                }
                byteBuffer.position(0)
            }
            else -> throw IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.javaClass)
        }
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }
}