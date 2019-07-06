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
            'f' -> parseFace(line)
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

    private fun parseFace(line: String) {
        parsePolygon(line.split(whitespaceRegex))
    }

    private fun parsePolygon(split: List<String>) {
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = currentIndices.size
        for (vertex in 0 until verticesCnt) {
            addVertex(split[vertex + 1])
            indices.add(nextIndex++)
        }
        val trianglesCnt = verticesCnt - 2
        for (triangle in 0 until trianglesCnt) {
            currentIndices.add(indices[0]) // triangle fan
            currentIndices.add(indices[triangle + 1])
            currentIndices.add(indices[triangle + 2])
        }
    }

    // Faces are defined using lists of vertex, texture and normal indices which start at 1
    private fun addVertex(vertex: String) {
        val vertSplit = vertex.split(slashRegex)
        val vertIndex = vertSplit[0].toInt() - 1
        val texIndex = vertSplit[1].toInt()  - 1
        val normIndex = vertSplit[2].toInt() - 1
        val vx = currentVertexList[vertIndex * 3 + 0]
        val vy = currentVertexList[vertIndex * 3 + 1]
        val vz = currentVertexList[vertIndex * 3 + 2]
        updateAABB(vx, vy, vz)
        currentVertices.add(vx)
        currentVertices.add(vy)
        currentVertices.add(vz)
        currentVertices.add(currentTexCoordList[texIndex  * 2 + 0])
        currentVertices.add(currentTexCoordList[texIndex  * 2 + 1])
        currentVertices.add(currentNormalList  [normIndex * 3 + 0])
        currentVertices.add(currentNormalList  [normIndex * 3 + 1])
        currentVertices.add(currentNormalList  [normIndex * 3 + 2])
    }

    private fun updateAABB(vx: Float, vy: Float, vz: Float) {
        if (vx < currentAABB.min.x) {
            currentAABB.min.x = vx
        }
        if (vx > currentAABB.max.x) {
            currentAABB.max.x = vx
        }
        if (vy < currentAABB.min.y) {
            currentAABB.min.y = vy
        }
        if (vy > currentAABB.max.y) {
            currentAABB.max.y = vy
        }
        if (vz < currentAABB.min.z) {
            currentAABB.min.z = vz
        }
        if (vz > currentAABB.max.z) {
            currentAABB.max.z = vz
        }
    }
}