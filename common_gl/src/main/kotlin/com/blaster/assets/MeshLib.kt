package com.blaster.assets

import com.blaster.auxiliary.aabb
import com.blaster.auxiliary.fail
import com.blaster.auxiliary.toByteBufferFloat
import com.blaster.auxiliary.toByteBufferInt
import com.blaster.gl.GlAttribute
import com.blaster.gl.GlBuffer
import com.blaster.gl.GlLocator
import com.blaster.gl.GlMesh
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

private val backend = GlLocator.locate()

private val whitespaceRegex = "\\s+".toRegex()
private val slashRegex = "/".toRegex()

private class Intermediate {
    val aabb = aabb()
    val positionList = mutableListOf<Float>()
    val texCoordList = mutableListOf<Float>()
    val normalList = mutableListOf<Float>()
    val positions = mutableListOf<Float>()
    val texCoords = mutableListOf<Float>()
    val normals = mutableListOf<Float>()
    val indicesList = mutableListOf<Int>()
}

// todo: info about model: vert/ind count, times, progress loading, etc
// todo: create Native(Float)Buffer directly, instead of copying arrays
class MeshLib (private val assetStream: AssetStream) {

    fun loadMesh(meshFilename: String): Pair<GlMesh, aabb> {
        // Storage for everything
        val result = Intermediate()
        // Reading file line by line
        val inputStream = assetStream.openAsset(meshFilename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                parseLine(line, result)
                line = bufferedReader.readLine()
            }
        }
        // Copying to appropriate buffers and sending to GPU memory
        val positionBuff = toByteBufferFloat(result.positions)
        val texCoordBuff = toByteBufferFloat(result.texCoords)
        val normalBuff = toByteBufferFloat(result.normals)
        val indicesBuff = toByteBufferInt(result.indicesList)
        val mesh = GlMesh(
            listOf(
                GlAttribute.ATTRIBUTE_POSITION to GlBuffer(backend.GL_ARRAY_BUFFER, positionBuff),
                GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer(backend.GL_ARRAY_BUFFER, texCoordBuff),
                GlAttribute.ATTRIBUTE_NORMAL to GlBuffer(backend.GL_ARRAY_BUFFER, normalBuff)
            ),
            GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, indicesBuff), result.indicesList.size
        )
        return mesh to result.aabb
    }

    private fun parseLine(line: String, result: Intermediate) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVertexAttribute(line, result)
            'f' -> parsePolygon(line, result)
        }
    }

    private fun parseVertexAttribute(line: String, result: Intermediate) {
        when (line[1]) {
            ' ' -> parsePosition(line, result)
            't' -> parseTexCoordinate(line, result)
            'n' -> parseNormal(line, result)
            else -> fail("Unknown vertex attribute! $line")
        }
    }

    private fun parsePosition(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.positionList.add(split[1].toFloat())
        result.positionList.add(split[2].toFloat())
        result.positionList.add(split[3].toFloat())
    }

    private fun parseTexCoordinate(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.texCoordList.add(split[1].toFloat())
        result.texCoordList.add(split[2].toFloat())
    }

    private fun parseNormal(line: String, result: Intermediate) {
        val split = line.split(whitespaceRegex)
        result.normalList.add(split[1].toFloat())
        result.normalList.add(split[2].toFloat())
        result.normalList.add(split[3].toFloat())
    }

    private fun parsePolygon(line: String, result: Intermediate) {
        // Splitting the line: ind1/ind2/ind3/...
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = result.positions.size / 3
        // Adding each vertex
        for (vertex in 0 until verticesCnt) {
            addVertex(split[vertex + 1], result)
            indices.add(nextIndex)
            nextIndex++
        }
        // Adding each triangle for the face
        val triangleCnt = verticesCnt - 2
        for (triangle in 0 until triangleCnt) {
            addTriangle(indices[0], indices[triangle + 1], indices[triangle + 2], result.indicesList)
        }
    }

    private fun addVertex(vertex: String, result: Intermediate) {
        val vertSplit = vertex.split(slashRegex)
        val vertIndex = vertSplit[0].toInt() - 1
        val vx = result.positionList[vertIndex * 3 + 0]
        val vy = result.positionList[vertIndex * 3 + 1]
        val vz = result.positionList[vertIndex * 3 + 2]
        result.positions.add(vx)
        result.positions.add(vy)
        result.positions.add(vz)
        updateAabb(result.aabb, vx, vy, vz)
        // In case if we do not have texture coordinates, just using 1,1
        if (result.texCoordList.isNotEmpty()) {
            val texIndex = vertSplit[1].toInt()  - 1
            result.texCoords.add(result.texCoordList[texIndex  * 2 + 0])
            result.texCoords.add(result.texCoordList[texIndex  * 2 + 1])
        } else {
            result.texCoords.add(1f)
            result.texCoords.add(1f)
        }
        // If we do not have normals, using up direction
        if (result.normalList.isNotEmpty()) {
            val normIndex = vertSplit[2].toInt() - 1
            result.normals.add(result.normalList[normIndex * 3 + 0])
            result.normals.add(result.normalList[normIndex * 3 + 1])
            result.normals.add(result.normalList[normIndex * 3 + 2])
        } else {
            result.normals.add(0f)
            result.normals.add(1f)
            result.normals.add(0f)
        }
    }

    private fun addTriangle(ind0: Int, ind1: Int, ind2: Int, indicesList: MutableList<Int>) {
        indicesList.add(ind0)
        indicesList.add(ind1)
        indicesList.add(ind2)
    }

    private fun updateAabb(aabb: aabb, vx: Float, vy: Float, vz: Float) {
        if (vx < aabb.minX) {
            aabb.minX = vx
        } else if (vx > aabb.maxX) {
            aabb.maxX = vx
        }
        if (vy < aabb.minY) {
            aabb.minY = vy
        } else if (vy > aabb.maxY) {
            aabb.maxY = vy
        }
        if (vz < aabb.minZ) {
            aabb.minZ = vz
        } else if (vz > aabb.maxZ) {
            aabb.maxZ = vz
        }
    }
}