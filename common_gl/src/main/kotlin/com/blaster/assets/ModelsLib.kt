package com.blaster.assets

import com.blaster.gl.GlAttribute
import com.blaster.gl.GlMesh
import com.blaster.scene.Model
import com.blaster.common.AABB
import org.joml.Vector3f
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

// todo: info about model: vert/ind count, times, progress loading, etc
// todo: load material from *.mtl
class ModelsLib (private val assetStream: AssetStream, private val texturesLib: TexturesLib) {
    private val whitespaceRegex = "\\s+".toRegex()
    private val slashRegex = "/".toRegex()

    private val currentVertexList = ArrayList<Float>()
    private val currentTexCoordList = ArrayList<Float>()
    private val currentNormalList = ArrayList<Float>()

    private val currentVertices = ArrayList<Float>()
    private val currentIndices = ArrayList<Int>()

    private var minX = 0f
    private var minY = 0f
    private var minZ = 0f
    private var maxX = 0f
    private var maxY = 0f
    private var maxZ = 0f

    // todo: create Native(Float)Buffer directly, instead of copying arrays
    fun loadModel(meshFilename: String, diffuseFilename: String): Model {
        minX = 0f
        minY = 0f
        minZ = 0f
        maxX = 0f
        maxY = 0f
        maxZ = 0f
        val inputStream = assetStream.openAsset(meshFilename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                parseLine(line)
                line = bufferedReader.readLine()
            }
        }
        val mesh = GlMesh(currentVertices.toFloatArray(), currentIndices.toIntArray(),
                listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD, GlAttribute.ATTRIBUTE_NORMAL))
        currentVertexList.clear()
        currentTexCoordList.clear()
        currentNormalList.clear()
        currentVertices.clear()
        currentIndices.clear()
        return Model(mesh, texturesLib.loadTexture(diffuseFilename), AABB(Vector3f(minX, minY, minZ), Vector3f(maxX, maxY, maxZ)))
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
        updateAabb(vx, vy, vz)
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

    private fun updateAabb(vx: Float, vy: Float, vz: Float) {
        if (vx < minX) {
            minX = vx
        }
        if (vx > maxX) {
            maxX = vx
        }
        if (vy < minY) {
            minY = vy
        }
        if (vy > maxY) {
            maxY = vy
        }
        if (vz < minZ) {
            minZ = vz
        }
        if (vz > maxZ) {
            maxZ = vz
        }
    }
}