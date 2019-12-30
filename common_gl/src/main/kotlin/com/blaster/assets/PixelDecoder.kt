package com.blaster.assets

import org.apache.commons.imaging.Imaging
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PixelDecoder {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    fun decodePixels(inputStream: InputStream): Decoded {
        val bufferedImage = Imaging.getBufferedImage(inputStream)
        val byteBuffer: ByteBuffer
        when (val dataBuffer = bufferedImage.raster.dataBuffer) {
            is DataBufferByte -> {
                val pixelData = dataBuffer.data
                byteBuffer = ByteBuffer.allocateDirect(pixelData.size)
                        .order(ByteOrder.nativeOrder())
                        .put(pixelData)
                byteBuffer.position(0)
            }
            is DataBufferInt -> {
                val pixelData = dataBuffer.data
                byteBuffer = ByteBuffer.allocateDirect(pixelData.size * 4)
                        .order(ByteOrder.nativeOrder())
                byteBuffer.asIntBuffer().put(pixelData)
                byteBuffer.position(0)
            }
            else -> throw IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.javaClass)
        }
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }
}