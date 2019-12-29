package com.blaster.assets

import java.io.InputStream
import java.nio.Buffer

interface PixelDecoder {
    data class Decoded(val pixels: Buffer, val width: Int, val height: Int)

    fun decodePixels(inputStream: InputStream): Decoded
}