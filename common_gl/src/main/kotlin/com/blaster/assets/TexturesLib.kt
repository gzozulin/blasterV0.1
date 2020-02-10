package com.blaster.assets

import com.blaster.entity.PbrMaterial
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
        // crazy cube map texture coordinates: https://stackoverflow.com/questions/11685608/convention-of-faces-in-opengl-cubemapping
        val right   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_rt.jpg"), mirrorX = true, mirrorY = true)
        val left    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_lf.jpg"), mirrorX = true, mirrorY = true)
        val top     = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_up.jpg"), mirrorX = true, mirrorY = true)
        val bottom  = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_dn.jpg"), mirrorX = true, mirrorY = true)
        val front   = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_ft.jpg"), mirrorX = true, mirrorY = true)
        val back    = pixelDecoder.decodePixels(assetStream.openAsset(filename + "/" + file.name + "_bk.jpg"), mirrorX = true, mirrorY = true)
        return GlTexture(unit = unit, sides = listOf(
                GlTexture.TexData(width = right.width,  height = right.height,  pixels = right.pixels),
                GlTexture.TexData(width = left.width,   height = left.height,   pixels = left.pixels),
                GlTexture.TexData(width = top.width,    height = top.height,    pixels = top.pixels),
                GlTexture.TexData(width = bottom.width, height = bottom.height, pixels = bottom.pixels),
                GlTexture.TexData(width = front.width,  height = front.height,  pixels = front.pixels),
                GlTexture.TexData(width = back.width,   height = back.height,   pixels = back.pixels)))
    }

    fun loadPbr(directory: String, extension: String = "png",
                albedo: String = "$directory/albedo.$extension",
                normal: String = "$directory/normal.$extension",
                metallic: String = "$directory/metallic.$extension",
                roughness: String = "$directory/roughness.$extension",
                ao: String = "$directory/ao.$extension"): PbrMaterial {
        val decodedAlbedo   = pixelDecoder.decodePixels(assetStream.openAsset(albedo))
        val decodedNormal   = pixelDecoder.decodePixels(assetStream.openAsset(normal))
        val decodedMetallic = pixelDecoder.decodePixels(assetStream.openAsset(metallic))
        val decodedRoughness = pixelDecoder.decodePixels(assetStream.openAsset(roughness))
        val decodedAo       = pixelDecoder.decodePixels(assetStream.openAsset(ao))
        return PbrMaterial(
                GlTexture(unit = 0, width = decodedAlbedo.width,    height = decodedAlbedo.height,    pixels = decodedAlbedo.pixels),
                GlTexture(unit = 1, width = decodedNormal.width,    height = decodedNormal.height,    pixels = decodedNormal.pixels),
                GlTexture(unit = 2, width = decodedMetallic.width,  height = decodedMetallic.height,  pixels = decodedMetallic.pixels),
                GlTexture(unit = 3, width = decodedRoughness.width, height = decodedRoughness.height, pixels = decodedRoughness.pixels),
                GlTexture(unit = 4, width = decodedAo.width,        height = decodedAo.height,        pixels = decodedAo.pixels))
    }
}