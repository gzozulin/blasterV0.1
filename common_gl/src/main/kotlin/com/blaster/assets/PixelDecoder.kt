package com.blaster.assets

import java.io.InputStream
import java.nio.ByteBuffer

interface PixelDecoder {
    data class Decoded(val pixels: ByteBuffer, val width: Int, val height: Int)

    fun decodePixels(inputStream: InputStream): Decoded
}