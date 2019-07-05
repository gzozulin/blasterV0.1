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

    private val currentVertexList = ArrayList<Float>()
    private val currentTexCoordList = ArrayList<Float>()
    private val currentNormalList = ArrayList<Float>()

    private val currentVertices = ArrayList<Float>()
    private val currentIndices = ArrayList<Int>()

    // todo create Ntive(Float)Buffer directly, instead of copying arrays
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
        val mesh = GLMesh(currentVertices.toFloatArray(), currentIndices.toIntArray(),
                listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD, GLAttribute.ATTRIBUTE_NORMAL))
        currentVertexList.clear()
        currentTexCoordList.clear()
        currentNormalList.clear()
        currentVertices.clear()
        currentIndices.clear()
        return GLModel(mesh, texturesLib.loadTexture(diffuseFilename), SceneNode())
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
        addIndex()
        addIndex()
        addIndex()
    }

    private fun parseQuadrilateral(split: List<String>) {
        addVertex(split[1].split(slashRegex))
        addVertex(split[2].split(slashRegex))
        addVertex(split[3].split(slashRegex))
        addVertex(split[4].split(slashRegex))
        val nextInd = currentIndices.size
        addIndex(nextInd + 0)
        addIndex(nextInd + 1)
        addIndex(nextInd + 3)
        addIndex(nextInd + 1)
        addIndex(nextInd + 2)
        addIndex(nextInd + 3)
    }

    private fun addVertex(vertSplit: List<String>) {
        // Faces are defined using lists of vertex, texture and normal indices which start at 1
        val vertIndex = vertSplit[0].toInt() - 1
        val texIndex = vertSplit[1].toInt() - 1
        val normIndex = vertSplit[2].toInt() - 1
        currentVertices.add(currentVertexList  [vertIndex * 3 + 0])
        currentVertices.add(currentVertexList  [vertIndex * 3 + 1])
        currentVertices.add(currentVertexList  [vertIndex * 3 + 2])
        currentVertices.add(currentTexCoordList[texIndex  * 2 + 0])
        currentVertices.add(currentTexCoordList[texIndex  * 2 + 1])
        currentVertices.add(currentNormalList  [normIndex * 3 + 0])
        currentVertices.add(currentNormalList  [normIndex * 3 + 1])
        currentVertices.add(currentNormalList  [normIndex * 3 + 2])
    }

    private fun addIndex(index: Int = currentIndices.size) {
        currentIndices.add(index)
    }
}