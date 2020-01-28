package com.blaster.assets

import com.blaster.gl.GlTexture
import java.io.File

class TexturesLib (
        private val assetStream: AssetStream,
        private val pixelDecoder: PixelDecoder = PixelDecoder()) {

    fun loadTexture(filename: String, unit: Int = 0, mirror: Boolean = false): GlTexture {
        val decoded = pixelDecoder.decodePixels(assetStream.openAsset(filename), mirror)
        return GlTexture(
                unit = unit,
                width = decoded.width, height = decoded.height,
                pixels = decoded.pixels)
    }

    fun loadSkybox(filename: String, unit: Int = 0): GlTexture {
        val file = File(filename)
        val right   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_rt.jpg"))
        val left    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_lf.jpg"))
        val top     = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_up.jpg"))
        val bottom  = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_dn.jpg"))
        val front   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_ft.jpg"))
        val back    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_bk.jpg"))
        return GlTexture(unit = unit, sides = listOf(
                GlTexture.TexData(width = right.width,  height = right.height,  pixels = right.pixels),
                GlTexture.TexData(width = left.width,   height = left.height,   pixels = left.pixels),
                GlTexture.TexData(width = top.width,    height = top.height,    pixels = top.pixels),
                GlTexture.TexData(width = bottom.width, height = bottom.height, pixels = bottom.pixels),
                GlTexture.TexData(width = front.width,  height = front.height,  pixels = front.pixels),
                GlTexture.TexData(width = back.width,   height = back.height,   pixels = back.pixels)
        ))
    }
}