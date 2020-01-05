package com.blaster.assets

import com.blaster.gl.GlProgram
import com.blaster.gl.GlShader
import com.blaster.gl.GlShaderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class ShadersLib(private val assetStream: AssetStream) {
    fun loadProgram(vertShaderAsset: String, fragShaderAsset: String) : GlProgram = GlProgram(
            GlShader(GlShaderType.VERTEX_SHADER, slurpAsset(vertShaderAsset)),
            GlShader(GlShaderType.FRAGMENT_SHADER, slurpAsset(fragShaderAsset)))

    private fun slurpAsset(filename: String): String {
        val stringBuilder = StringBuilder()
        val inputStream = assetStream.openAsset(filename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append("$line\n")
                line = bufferedReader.readLine()
            }
        }
        return stringBuilder.toString()
    }
}