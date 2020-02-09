package com.blaster.entity

import com.blaster.gl.GlTexture

data class PbrMaterial(
        val albedo: GlTexture, val normal: GlTexture, val metallic: GlTexture,
        val roughness: GlTexture, val ao: GlTexture)