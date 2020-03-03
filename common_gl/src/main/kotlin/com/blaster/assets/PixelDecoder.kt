package com.blaster.assets

import java.awt.Color
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

open class PixelDecoder {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    open fun decodePixels(inputStream: InputStream, mirrorX: Boolean = false, mirrorY: Boolean = false): Decoded {
        val bufferedImage = ImageIO.read(inputStream)
        val pixelNum = bufferedImage.width * bufferedImage.height
        val byteBuffer = ByteBuffer.allocateDirect(pixelNum * 4).order(ByteOrder.nativeOrder())
        val xRange = if (mirrorX) {
            bufferedImage.width - 1 downTo 0
        } else {
            0 until bufferedImage.width
        }
        val yRange = if (mirrorY) {
            0 until bufferedImage.height
        } else {
            bufferedImage.height - 1 downTo 0
        }
        for (y in yRange) {
            for (x in xRange) {
                val color = Color(bufferedImage.getRGB(x, y), true)
                addColor(color, byteBuffer)
            }
        }
        byteBuffer.position(0)
        return Decoded(byteBuffer, bufferedImage.width, bufferedImage.height)
    }

    private fun addColor(color: Color, byteBuffer: ByteBuffer) {
        byteBuffer.put(color.red.toByte())
        byteBuffer.put(color.green.toByte())
        byteBuffer.put(color.blue.toByte())
        byteBuffer.put(color.alpha.toByte())
    }
}