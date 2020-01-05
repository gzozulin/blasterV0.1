package com.blaster.assets

import com.blaster.gl.GlTexture

class TexturesLib (private val assetStream: AssetStream, private val pixelDecoder: PixelDecoder) {
    fun loadTexture(filename: String, unit: Int = 0): GlTexture {
        val decoded = pixelDecoder.decodePixels(assetStream.openAsset(filename))
        val tex = GlTexture(
                unit = unit,
                width = decoded.width, height = decoded.height,
                pixels = decoded.pixels)
        return tex
    }
}