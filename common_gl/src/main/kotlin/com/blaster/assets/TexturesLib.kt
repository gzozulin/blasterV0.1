package com.blaster.assets

import com.blaster.gl.GLTexture

class TexturesLib (private val assetStream: AssetStream, private val pixelDecoder: PixelDecoder) {
    fun loadTexture(filename: String, unit: Int = 0): GLTexture {
        val decoded = pixelDecoder.decodePixels(assetStream.openAsset(filename))
        val tex = GLTexture(
                unit = unit,
                width = decoded.width, height = decoded.height,
                pixels = decoded.pixels)
        return tex
    }
}