package com.gzozulin.wallpaper

import android.content.Context
import com.gzozulin.wallpaper.gl.GLProgram
import com.gzozulin.wallpaper.gl.GLShader
import com.gzozulin.wallpaper.gl.ShaderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class ShaderLib(private val ctx: Context) {
    fun loadShader(vertShaderSrc: String, fragShaderSrc: String) : GLProgram =
            GLProgram(
                GLShader(ShaderType.VERTEX_SHADER, slurpAsset(vertShaderSrc)),
                GLShader(ShaderType.FRAGMENT_SHADER, slurpAsset(fragShaderSrc)))

    private fun slurpAsset(filename: String): String {
        val stringBuilder = StringBuilder()
        val inputStream = ctx.assets.open(filename)
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