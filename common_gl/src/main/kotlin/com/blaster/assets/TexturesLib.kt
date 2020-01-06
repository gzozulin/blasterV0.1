package com.blaster.assets

import com.blaster.gl.GlTexture

class TexturesLib (
        private val assetStream: AssetStream,
        private val pixelDecoder: PixelDecoder = PixelDecoder()) {
    fun loadTexture(filename: String, unit: Int = 0): GlTexture {
        val decoded = pixelDecoder.decodePixels(assetStream.openAsset(filename))
        return GlTexture(
                unit = unit,
                width = decoded.width, height = decoded.height,
                pixels = decoded.pixels)
    }
}