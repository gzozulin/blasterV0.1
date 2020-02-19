package com.blaster.assets

import com.blaster.aux.aabb
import com.blaster.aux.fail
import com.blaster.aux.toByteBufferFloat
import com.blaster.aux.toByteBufferInt
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

// todo: info about model: vert/ind count, times, progress loading, etc
// todo: create Native(Float)Buffer directly, instead of copying arrays
class MeshLib (private val assetStream: AssetStream) {

    fun loadMesh(meshFilename: String): Pair<GlMesh, aabb> {
        // Storage for everything
        val aabb = aabb()
        val positionList = mutableListOf<Float>()
        val texCoordList = mutableListOf<Float>()
        val normalList = mutableListOf<Float>()
        val positions = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val indicesList = mutableListOf<Int>()
        // Reading file line by line
        val inputStream = assetStream.openAsset(meshFilename)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {
                parseLine(aabb, line, positionList, texCoordList, normalList,
                        positions, texCoords, normals, indicesList)
                line = bufferedReader.readLine()
            }
        }
        // Copying to appropriate buffers and sending to GPU memory
        val positionBuff = toByteBufferFloat(positions)
        val texCoordBuff = toByteBufferFloat(texCoords)
        val normalBuff = toByteBufferFloat(normals)
        val indicesBuff = toByteBufferInt(indicesList)
        val mesh = GlMesh(
            listOf(
                GlAttribute.ATTRIBUTE_POSITION to GlBuffer(backend.GL_ARRAY_BUFFER, positionBuff),
                GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer(backend.GL_ARRAY_BUFFER, texCoordBuff),
                GlAttribute.ATTRIBUTE_NORMAL to GlBuffer(backend.GL_ARRAY_BUFFER, normalBuff)
            ),
            GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, indicesBuff), indicesList.size
        )
        return mesh to aabb
    }

    private fun parseLine(aabb: aabb, line: String,
                          positionList: MutableList<Float>,
                          texCoordList: MutableList<Float>,
                          normalList: MutableList<Float>,
                          positions: MutableList<Float>,
                          texCoords: MutableList<Float>,
                          normals: MutableList<Float>,
                          indicesList: MutableList<Int>) {
        if (line.isEmpty()) {
            return
        }
        when (line[0]) {
            'v' -> parseVertexAttribute(line, positionList, texCoordList, normalList)
            'f' -> parsePolygon(aabb, line, positionList, texCoordList, normalList,
                    positions, texCoords, normals, indicesList)
        }
    }

    private fun parseVertexAttribute(line: String,
                                     positionList: MutableList<Float>,
                                     texCoordList: MutableList<Float>,
                                     normalList: MutableList<Float>) {
        when (line[1]) {
            ' ' -> parsePosition(line, positionList)
            't' -> parseTexCoordinate(line, texCoordList)
            'n' -> parseNormal(line, normalList)
            else -> fail("Unknown vertex attribute! $line")
        }
    }

    private fun parsePosition(line: String, positionList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        positionList.add(split[1].toFloat())
        positionList.add(split[2].toFloat())
        positionList.add(split[3].toFloat())
    }

    private fun parseTexCoordinate(line: String, texCoordList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        texCoordList.add(split[1].toFloat())
        texCoordList.add(split[2].toFloat())
    }

    private fun parseNormal(line: String, normalList: MutableList<Float>) {
        val split = line.split(whitespaceRegex)
        normalList.add(split[1].toFloat())
        normalList.add(split[2].toFloat())
        normalList.add(split[3].toFloat())
    }

    private fun parsePolygon(aabb: aabb, line: String,
                             positionList: List<Float>,
                             texCoordList: List<Float>,
                             normalList: List<Float>,
                             positions: MutableList<Float>,
                             texCoords: MutableList<Float>,
                             normals: MutableList<Float>,
                             indicesList: MutableList<Int>) {
        // Splitting the line: ind1/ind2/ind3/...
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = positions.size / 3
        // Adding each vertex
        for (vertex in 0 until verticesCnt) {
            addVertex(aabb, split[vertex + 1],
                    positionList, texCoordList, normalList, positions, texCoords, normals)
            indices.add(nextIndex)
            nextIndex++
        }
        // Adding each triangle for the face
        val triangleCnt = verticesCnt - 2
        for (triangle in 0 until triangleCnt) {
            addTriangle(indices[0], indices[triangle + 1], indices[triangle + 2], indicesList)
        }
    }

    private fun addVertex(aabb: aabb, vertex: String,
                          positionList: List<Float>,
                          texCoordList: List<Float>,
                          normalList: List<Float>,
                          positions: MutableList<Float>,
                          texCoords: MutableList<Float>,
                          normals: MutableList<Float>) {
        val vertSplit = vertex.split(slashRegex)
        val vertIndex = vertSplit[0].toInt() - 1
        val vx = positionList[vertIndex * 3 + 0]
        val vy = positionList[vertIndex * 3 + 1]
        val vz = positionList[vertIndex * 3 + 2]
        positions.add(vx)
        positions.add(vy)
        positions.add(vz)
        updateAabb(aabb, vx, vy, vz)
        // In case if we do not have texture coordinates, just using 1,1
        if (texCoordList.isNotEmpty()) {
            val texIndex = vertSplit[1].toInt()  - 1
            texCoords.add(texCoordList[texIndex  * 2 + 0])
            texCoords.add(texCoordList[texIndex  * 2 + 1])
        } else {
            texCoords.add(1f)
            texCoords.add(1f)
        }
        // If we do not have normals, using up direction
        if (normalList.isNotEmpty()) {
            val normIndex = vertSplit[2].toInt() - 1
            normals.add(normalList[normIndex * 3 + 0])
            normals.add(normalList[normIndex * 3 + 1])
            normals.add(normalList[normIndex * 3 + 2])
        } else {
            normals.add(0f)
            normals.add(1f)
            normals.add(0f)
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