package com.blaster.texture

import com.blaster.Texture
import com.blaster.math.Vec3
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO

fun Int.bytes(): ByteBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(this)

data class ImageTexture(val filename: String) : Texture {
    private val image: BufferedImage = ImageIO.read(File(filename))

    private val width = image.width
    private val height = image.height

    override fun value(u: Float, v: Float, point: Vec3): Vec3 {
        val x = u * (width - 1)
        val y = (1f - v) * (height - 1)
        val pixel = image.getRGB(x.toInt(), y.toInt())
        val buffer = pixel.bytes()
        return Vec3(
            (buffer[1].toInt() and 0xFF).toFloat() / 255f,
            (buffer[2].toInt() and 0xFF).toFloat() / 255f,
            (buffer[3].toInt() and 0xFF).toFloat() / 255f
        )
    }
}