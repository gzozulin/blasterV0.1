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
                val pixelData = dataBuffer.data
                byteBuffer = ByteBuffer.allocateDirect(pixelData.size)
                byteBuffer.put(pixelData)
                byteBuffer.position(0)
            }
            else -> throw IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.javaClass)
        }
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }
}