package com.blaster.assets

import java.awt.Color
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

open class PixelDecoder {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    open fun decodePixels(inputStream: InputStream): Decoded {
        val bufferedImage = ImageIO.read(inputStream)
        val pixelNum = bufferedImage.width * bufferedImage.height
        val byteBuffer = ByteBuffer.allocateDirect(pixelNum * 4).order(ByteOrder.nativeOrder())
        for (y in 0 until bufferedImage.height) {
            for (x in 0 until bufferedImage.width) {
                val color = Color(bufferedImage.getRGB(x, y))
                byteBuffer.put(color.red.toByte())
                byteBuffer.put(color.green.toByte())
                byteBuffer.put(color.blue.toByte())
                byteBuffer.put(color.alpha.toByte())
            }
        }
        byteBuffer.position(0)
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }
}