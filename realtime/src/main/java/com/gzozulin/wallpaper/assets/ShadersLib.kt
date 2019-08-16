package com.gzozulin.wallpaper.assets

import android.content.Context
import com.gzozulin.wallpaper.gl.GLProgram
import com.gzozulin.wallpaper.gl.GLShader
import com.gzozulin.wallpaper.gl.GLShaderType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class ShadersLib(private val ctx: Context) {
    fun loadProgram(vertShaderAsset: String, fragShaderAsset: String) : GLProgram = GLProgram(
                GLShader(GLShaderType.VERTEX_SHADER, slurpAsset(vertShaderAsset)),
                GLShader(GLShaderType.FRAGMENT_SHADER, slurpAsset(fragShaderAsset)))

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