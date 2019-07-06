package com.gzozulin.wallpaper.assets

import android.content.Context
import com.gzozulin.wallpaper.gl.GLAttribute
import com.gzozulin.wallpaper.gl.GLMesh
import com.gzozulin.wallpaper.gl.GLModel
import com.gzozulin.wallpaper.math.AABB
import com.gzozulin.wallpaper.math.SceneNode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files.size

// todo: info about model: vert/ind count, times, progress loading, etc
class ModelsLib (private val ctx: Context, private val texturesLib: TexturesLib) {
    private val whitespaceRegex = "\\s+".toRegex()
    private val slashRegex = "/".toRegex()

    private val currentVertexList = ArrayList<Float>()
    private val currentTexCoordList = ArrayList<Float>()
    private val currentNormalList = ArrayList<Float>()

    private val currentVertices = ArrayList<Float>()
    private val currentIndices = ArrayList<Int>()

    private lateinit var currentAABB: AABB

    // todo: create Native(Float)Buffer directly, instead of copying arrays
    fun loadModel(meshFilename: String, diffuseFilename: String): GLModel {
        currentAABB = AABB()
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
        return GLModel(mesh, texturesLib.loadTexture(diffuseFilename), SceneNode(), currentAABB)
    }

    private fun parseLine(line: String) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVertexAttribute(line)
            'f' -> parsePolygon(line)
        }
    }

    private fun parseVertexAttribute(line: String) {
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
        currentTexCoordList.add(1f - split[2].toFloat())
    }

    private fun parseNormal(line: String) {
        val split = line.split(whitespaceRegex)
        currentNormalList.add(split[1].toFloat())
        currentNormalList.add(split[2].toFloat())
        currentNormalList.add(split[3].toFloat())
    }

    private fun parsePolygon(line: String) {
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = currentVertices.size / 8 // position, texcoord, normal
        for (vertex in 0 until verticesCnt) {
            addVertex(split[vertex + 1])
            indices.add(nextIndex)
            nextIndex++
        }
        val triangleCnt = verticesCnt - 2
        for (triangle in 0 until triangleCnt) {
            addTriangle(indices[0], indices[triangle + 1], indices[triangle + 2])
        }
    }

    // Faces are defined using lists of vertex, texture and normal indices which start at 1
    private fun addVertex(vertex: String) {
        val vertSplit = vertex.split(slashRegex)
        val vertIndex = vertSplit[0].toInt() - 1
        val vx = currentVertexList[vertIndex * 3 + 0]
        val vy = currentVertexList[vertIndex * 3 + 1]
        val vz = currentVertexList[vertIndex * 3 + 2]
        currentVertices.add(vx)
        currentVertices.add(vy)
        currentVertices.add(vz)
        currentAABB.include(vx, vy, vz)
        if (vertSplit[1].isNotEmpty()) {
            val texIndex = vertSplit[1].toInt()  - 1
            currentVertices.add(currentTexCoordList[texIndex  * 2 + 0])
            currentVertices.add(currentTexCoordList[texIndex  * 2 + 1])
        } else {
            currentVertices.add(0f)
            currentVertices.add(0f)
        }
        if (vertSplit[2].isNotEmpty()) {
            val normIndex = vertSplit[2].toInt() - 1
            currentVertices.add(currentNormalList  [normIndex * 3 + 0])
            currentVertices.add(currentNormalList  [normIndex * 3 + 1])
            currentVertices.add(currentNormalList  [normIndex * 3 + 2])
        } else {
            currentVertices.add(0f)
            currentVertices.add(0f)
            currentVertices.add(0f)
        }
    }

    private fun addTriangle(ind0: Int, ind1: Int, ind2: Int) {
        currentIndices.add(ind0)
        currentIndices.add(ind1)
        currentIndices.add(ind2)
    }
}