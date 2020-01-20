package com.blaster.assets

import com.blaster.gl.GlAttribute
import com.blaster.gl.GlBuffer
import com.blaster.gl.GlLocator
import com.blaster.scene.Mesh
import com.blaster.scene.Model
import org.joml.AABBf
import org.joml.Vector3f
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

private val backend = GlLocator.locate()

private fun arrayListFloatToByteBuffer(list: List<Float>): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asFloatBuffer()
    list.forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}

private fun arrayListIntToByteBuffer(list: List<Int>): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(list.size * 4).order(ByteOrder.nativeOrder())
    val typed = buffer.asIntBuffer()
    list.forEach { typed.put(it) }
    buffer.position(0)
    return buffer
}

// todo: scaler - to bring any model to the scene dimensions
// todo: info about model: vert/ind count, times, progress loading, etc
// todo: load material from *.mtl
// todo: use buffers directly
class ModelsLib (private val assetStream: AssetStream, private val texturesLib: TexturesLib) {
    private val whitespaceRegex = "\\s+".toRegex()
    private val slashRegex = "/".toRegex()

    private val currentPositionList = mutableListOf<Float>()
    private val currentTexCoordList = mutableListOf<Float>()
    private val currentNormalList = mutableListOf<Float>()

    private val currentPositions = mutableListOf<Float>()
    private val currentTexCoords = mutableListOf<Float>()
    private val currentNormals = mutableListOf<Float>()
    private val currentIndices = mutableListOf<Int>()

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
        val positionBuff = arrayListFloatToByteBuffer(currentPositions)
        val texCoordBuff = arrayListFloatToByteBuffer(currentTexCoords)
        val normalBuff = arrayListFloatToByteBuffer(currentNormals)
        val indicesBuff = arrayListIntToByteBuffer(currentIndices)
        val mesh = Mesh(
                listOf(
                        GlAttribute.ATTRIBUTE_POSITION to GlBuffer(backend.GL_ARRAY_BUFFER, positionBuff),
                        GlAttribute.ATTRIBUTE_TEXCOORD to GlBuffer(backend.GL_ARRAY_BUFFER, texCoordBuff),
                        GlAttribute.ATTRIBUTE_NORMAL to GlBuffer(backend.GL_ARRAY_BUFFER, normalBuff)
                ),
                GlBuffer(backend.GL_ELEMENT_ARRAY_BUFFER, indicesBuff), currentIndices.size
        )
        currentPositionList.clear()
        currentTexCoordList.clear()
        currentNormalList.clear()
        currentPositions.clear()
        currentTexCoords.clear()
        currentNormals.clear()
        currentIndices.clear()
        return Model(mesh, texturesLib.loadTexture(diffuseFilename),
                AABBf(Vector3f(minX, minY, minZ), Vector3f(maxX, maxY, maxZ)))
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
            ' ' -> parsePosition(line)
            't' -> parseTexCoordinate(line)
            'n' -> parseNormal(line)
            else -> throw IllegalStateException("Unknown vertex attribute! $line")
        }
    }

    private fun parsePosition(line: String) {
        val split = line.split(whitespaceRegex)
        currentPositionList.add(split[1].toFloat())
        currentPositionList.add(split[2].toFloat())
        currentPositionList.add(split[3].toFloat())
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

    private fun parsePolygon(line: String) {
        val split = line.split(whitespaceRegex)
        val verticesCnt = split.size - 1
        val indices = ArrayList<Int>()
        var nextIndex = currentPositions.size / 3 // x, y, z
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
        val vx = currentPositionList[vertIndex * 3 + 0]
        val vy = currentPositionList[vertIndex * 3 + 1]
        val vz = currentPositionList[vertIndex * 3 + 2]
        currentPositions.add(vx)
        currentPositions.add(vy)
        currentPositions.add(vz)
        updateAabb(vx, vy, vz)
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