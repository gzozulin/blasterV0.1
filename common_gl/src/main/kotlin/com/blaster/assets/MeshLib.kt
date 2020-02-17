package com.blaster.assets

import com.blaster.aux.aabb
import com.blaster.aux.fail
import com.blaster.aux.toByteBufferFloat
import com.blaster.aux.toByteBufferInt
import com.blaster.gl.GlAttribute
import com.blaster.gl.GlBuffer
import com.blaster.gl.GlLocator
import com.blaster.gl.GlMesh
import org.joml.AABBf
import org.joml.Vector3f
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

private val backend = GlLocator.locate()

private val whitespaceRegex = "\\s+".toRegex()
private val slashRegex = "/".toRegex()

// todo: info about model: vert/ind count, times, progress loading, etc
// todo: create Native(Float)Buffer directly, instead of copying arrays
class MeshLib (private val assetStream: AssetStream) {

    fun loadMesh(meshFilename: String): Pair<GlMesh, AABBf> {
        val aabb = aabb()
        val currentPositionList = mutableListOf<Float>()
        val currentTexCoordList = mutableListOf<Float>()
        val currentNormalList = mutableListOf<Float>()
        val currentPositions = mutableListOf<Float>()
        val currentTexCoords = mutableListOf<Float>()
        val currentNormals = mutableListOf<Float>()
        val currentIndices = mutableListOf<Int>()
        val inputStream = assetStream.openAsset(meshFilename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                parseLine(aabb, line, currentPositionList, currentTexCoordList, currentNormalList,
                        currentPositions, currentTexCoords, currentNormals, currentIndices)
                line = bufferedReader.readLine()
            }
        }
        val positionBuff = toByteBufferFloat(currentPositions)
        val texCoordBuff = toByteBufferFloat(currentTexCoords)
        val normalBuff = toByteBufferFloat(currentNormals)
        val indicesBuff = toByteBufferInt(currentIndices)
        val mesh = GlMesh(
                listOf(
                        GlAttribute.ATTRIBUTE_POSITION to GlBuffer(backend.GL_ARRAY_BUFFER, positionBuff),
                        GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer(backend.GL_ARRAY_BUFFER, texCoordBuff),
                        GlAttribute.ATTRIBUTE_NORMAL to GlBuffer(backend.GL_ARRAY_BUFFER, normalBuff)
                ),
                GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, indicesBuff), currentIndices.size
        )
        return mesh to AABBf(aabb)
    }

    private fun parseLine(aabb: aabb, line: String,
                          currentPositionList: MutableList<Float>,
                          currentTexCoordList: MutableList<Float>,
                          currentNormalList: MutableList<Float>,
                          currentPositions: MutableList<Float>,
                          currentTexCoords: MutableList<Float>,
                          currentNormals: MutableList<Float>,
                          currentIndices: MutableList<Int>) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVertexAttribute(line, currentPositionList, currentTexCoordList, currentNormalList)
            'f' -> parsePolygon(aabb, line, currentPositionList, currentTexCoordList, currentNormalList,
                    currentPositions, currentTexCoords, currentNormals, currentIndices)
        }
    }

    private fun parseVertexAttribute(line: String,
                                     currentPositionList: MutableList<Float>,
                                     currentTexCoordList: MutableList<Float>,
                                     currentNormalList: MutableList<Float>) {
        when (line[1]) {
            ' ' -> parsePosition(line, currentPositionList)
            't' -> parseTexCoordinate(line, currentTexCoordList)
            'n' -> parseNormal(line, currentNormalList)
            else -> fail("Unknown vertex attribute! $line")
        }
    }

    private fun parsePosition(line: String, currentPositionList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        currentPositionList.add(split[1].toFloat())
        currentPositionList.add(split[2].toFloat())
        currentPositionList.add(split[3].toFloat())
    }

    private fun parseTexCoordinate(line: String, currentTexCoordList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        currentTexCoordList.add(split[1].toFloat())
        currentTexCoordList.add(split[2].toFloat())
    }

    private fun parseNormal(line: String, currentNormalList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        currentNormalList.add(split[1].toFloat())
        currentNormalList.add(split[2].toFloat())
        currentNormalList.add(split[3].toFloat())
    }

    private fun parsePolygon(aabb: aabb, line: String,
                             currentPositionList: List<Float>,
                             currentTexCoordList: List<Float>,
                             currentNormalList: List<Float>,
                             currentPositions: MutableList<Float>,
                             currentTexCoords: MutableList<Float>,
                             currentNormals: MutableList<Float>,
                             currentIndices: MutableList<Int>) {
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = currentPositions.size / 3
        for (vertex in 0 until verticesCnt) {
            addVertex(aabb, split[vertex + 1], currentPositionList, currentTexCoordList,
                    currentNormalList, currentPositions, currentTexCoords, currentNormals)
            indices.add(nextIndex)
            nextIndex++
        }
        val triangleCnt = verticesCnt - 2
        for (triangle in 0 until triangleCnt) {
            addTriangle(indices[0], indices[triangle + 1], indices[triangle + 2], currentIndices)
        }
    }

    private fun addVertex(aabb: aabb, vertex: String,
                          currentPositionList: List<Float>,
                          currentTexCoordList: List<Float>,
                          currentNormalList: List<Float>,
                          currentPositions: MutableList<Float>,
                          currentTexCoords: MutableList<Float>,
                          currentNormals: MutableList<Float>) {
        val vertSplit = vertex.split(slashRegex)
        val vertIndex = vertSplit[0].toInt() - 1
        val vx = currentPositionList[vertIndex * 3 + 0]
        val vy = currentPositionList[vertIndex * 3 + 1]
        val vz = currentPositionList[vertIndex * 3 + 2]
        currentPositions.add(vx)
        currentPositions.add(vy)
        currentPositions.add(vz)
        updateAabb(aabb, vx, vy, vz)
        if (currentTexCoordList.isNotEmpty()) {
            val texIndex = vertSplit[1].toInt()  - 1
            currentTexCoords.add(currentTexCoordList[texIndex  * 2 + 0])
            currentTexCoords.add(currentTexCoordList[texIndex  * 2 + 1])
        } else {
            currentTexCoords.add(1f)
            currentTexCoords.add(1f)
        }
        if (currentNormalList.isNotEmpty()) {
            val normIndex = vertSplit[2].toInt() - 1
            currentNormals.add(currentNormalList  [normIndex * 3 + 0])
            currentNormals.add(currentNormalList  [normIndex * 3 + 1])
            currentNormals.add(currentNormalList  [normIndex * 3 + 2])
        } else {
            currentNormals.add(0f)
            currentNormals.add(1f)
            currentNormals.add(0f)
        }
    }

    private fun addTriangle(ind0: Int, ind1: Int, ind2: Int, currentIndices: MutableList<Int>) {
        currentIndices.add(ind0)
        currentIndices.add(ind1)
        currentIndices.add(ind2)
    }

    private fun updateAabb(aabb: aabb, vx: Float, vy: Float, vz: Float) {
        if (vx < aabb.minX) {
            aabb.minX = vx
        }
        if (vx > aabb.maxX) {
            aabb.maxX = vx
        }
        if (vy < aabb.minY) {
            aabb.minY = vy
        }
        if (vy > aabb.maxY) {
            aabb.maxY = vy
        }
        if (vz < aabb.minZ) {
            aabb.minZ = vz
        }
        if (vz > aabb.maxZ) {
            aabb.maxZ = vz
        }
    }
}