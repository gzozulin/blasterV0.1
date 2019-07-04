package com.gzozulin.wallpaper.assets

import android.content.Context
import com.gzozulin.wallpaper.gl.GLAttribute
import com.gzozulin.wallpaper.gl.GLMesh
import com.gzozulin.wallpaper.gl.GLModel
import com.gzozulin.wallpaper.math.SceneNode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class ModelsLib (private val ctx: Context, private val texturesLib: TexturesLib) {
    private val whitespaceRegex = "\\s+".toRegex()
    private val slashRegex = "/".toRegex()

    private val attributesPerVertex =
            GLAttribute.ATTRIBUTE_POSITION.size +
            GLAttribute.ATTRIBUTE_TEXCOORD.size +
            GLAttribute.ATTRIBUTE_NORMAL.size

    private val currentVertexList = ArrayList<Float>()
    private val currentTexCoordList = ArrayList<Float>()
    private val currentNormalList = ArrayList<Float>()

    private val vertices = ArrayList<Float>()

    fun loadModel(meshFilename: String, diffuseFilename: String): GLModel {
        val inputStream = ctx.assets.open(meshFilename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                parseLine(line)
                line = bufferedReader.readLine()
            }
        }
        val verticesArray = vertices.toFloatArray()
        val verticesCnt = verticesArray.size / attributesPerVertex
        val indicesArray = IntArray(verticesCnt)
        for (i in 0 until verticesCnt) {
            indicesArray[i] = i
        }
        val mesh = GLMesh(verticesArray, indicesArray,
                listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD, GLAttribute.ATTRIBUTE_NORMAL))
        val diffuse = texturesLib.loadTexture(diffuseFilename)
        currentVertexList.clear()
        currentTexCoordList.clear()
        currentNormalList.clear()
        vertices.clear()
        return GLModel(mesh, diffuse, SceneNode())
    }

    private fun parseLine(line: String) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVectorAttribute(line)
            'f' -> parseFace(line)
        }
    }

    private fun parseVectorAttribute(line: String) {
        when (line[1]) {
            ' ' -> parseVertex(line)
            't' -> parseTexCoordinate(line)
            'n' -> parseNormal(line)
            else -> throw IllegalStateException("Unknown vertex attribute! $line")
        }
    }

    private fun parseVertex(line: String) {
        val split = line.split(whitespaceRegex)
        currentVertexList.add(split[1].toFloat())
        currentVertexList.add(split[2].toFloat())
        currentVertexList.add(split[3].toFloat())
    }

    private fun parseTexCoordinate(line: String) {
        val split = line.split(whitespaceRegex)
        currentTexCoordList.add(split[1].toFloat())
        currentTexCoordList.add(split[2].toFloat())
    }

    private fun parseNormal(line: String) {
        val split = line.split(whitespaceRegex)
        currentNormalList.add(split[1].toFloat())
        currentNormalList.add(split[2].toFloat())
        currentNormalList.add(split[3].toFloat())
    }

    private fun parseFace(line: String) {
        val split = line.split(whitespaceRegex)
        when (split.size) {
            4 -> parseTriangle(split)
            5 -> parseQuadrilateral(split)
            else -> throw IllegalStateException("Unknown geometry type! $line")
        }
    }

    private fun parseTriangle(split: List<String>) {
        addVertex(split[1].split(slashRegex))
        addVertex(split[2].split(slashRegex))
        addVertex(split[3].split(slashRegex))
    }

    private fun parseQuadrilateral(split: List<String>) {
        addVertex(split[1].split(slashRegex))
        addVertex(split[2].split(slashRegex))
        addVertex(split[4].split(slashRegex))
        addVertex(split[2].split(slashRegex))
        addVertex(split[3].split(slashRegex))
        addVertex(split[4].split(slashRegex))
    }

    private fun addVertex(split: List<String>) {
        val vertIndex = split[0].toInt()
        val texIndex = split[1].toInt()
        val normIndex = split[2].toInt()
        vertices.add(currentVertexList  [vertIndex + 0])
        vertices.add(currentVertexList  [vertIndex + 1])
        vertices.add(currentVertexList  [vertIndex + 2])
        vertices.add(currentTexCoordList[texIndex  + 0])
        vertices.add(currentTexCoordList[texIndex  + 1])
        vertices.add(currentNormalList  [normIndex + 0])
        vertices.add(currentNormalList  [normIndex + 1])
        vertices.add(currentNormalList  [normIndex + 2])
    }
}