package com.blaster.scene

import com.blaster.common.aabb
import com.blaster.common.euler3
import com.blaster.common.quat
import com.blaster.common.vec3
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

// todo: toLeftOf, toRightOf, toTopOf, toBottomOf, toFrontOf, toBackOf - by aabb (which is always axis aligned)

// todo: vec3, euler, quat as one val

// todo: probably, also can have matrix directly

class SceneReader {
    fun load(sceneString: String) = load(sceneStream = sceneString.byteInputStream(StandardCharsets.UTF_8))

    fun load(sceneStream: InputStream): List<Marker> {
        val lines = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
                .readLines().toMutableList()
        return parse(0, lines)
    }

    private fun peek(input: String): Int {
        var count = 0
        while (input[count] == ' ') {
            count++
        }
        return count
    }

    private fun parse(depth: Int, remainder: MutableList<String>): List<Marker> {
        val result = mutableListOf<Marker>()
        loop@ while (remainder.isNotEmpty()) {
            val currentDepth = peek(remainder[0])
            when {
                currentDepth == depth -> result.add(parseMarker(remainder.removeAt(0)))
                currentDepth > depth -> result.last().children.addAll(parse(currentDepth, remainder))
                currentDepth < depth -> break@loop
            }
        }
        return result
    }

    private fun parseMarker(marker: String): Marker {
        val tokens = marker.trim().split(Pattern.compile(";\\s*"))
        val uid: String = tokens[0]
        var pos: vec3? = null
        var quat: quat? = null
        var euler: euler3? = null
        var scale: vec3? = null
        var aabb: vec3? = null
        var dir: vec3? = null
        var target: String? = null
        var custom: String? = null
        tokens.forEach {
            when {
                it.startsWith("pos") -> pos = parseVec3(it.removePrefix("pos").trim())
                it.startsWith("quat") -> quat = parseQuat(it.removePrefix("quat").trim())
                it.startsWith("euler") -> euler = parseVec3(it.removePrefix("euler").trim())
                it.startsWith("scale") -> scale = parseVec3(it.removePrefix("scale").trim())
                it.startsWith("aabb") -> aabb = parseVec3(it.removePrefix("aabb").trim())
                it.startsWith("dir") -> dir = parseVec3(it.removePrefix("dir").trim())
                it.startsWith("target") -> target = it.removePrefix("target").trim()
                it.startsWith("custom") -> custom = it.removePrefix("custom").trim()
            }
        }
        return Marker(uid, pos!!, euler, quat, scale, aabb, dir, target, custom, mutableListOf())
    }

    // todo: a little bit of parsing inefficiency down there:

    private fun parseVec3(value: String): vec3 {
        val tokens = value.split(Pattern.compile("\\s+"))
        return when (tokens.size) {
            3 -> vec3(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat())
            1 -> vec3(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
            else -> throw IllegalArgumentException()
        }
    }

    private fun parseQuat(value: String): quat {
        val tokens = value.split(Pattern.compile("\\s+"))
        return when (tokens.size) {
            4 -> quat(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
            1 -> quat(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
            else -> throw IllegalArgumentException()
        }
    }

    private fun parseAabb(value: String): aabb {
        val tokens = value.split(Pattern.compile("\\s+"))
        return when (tokens.size) {
            6 -> aabb(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat(), tokens[4].toFloat(), tokens[5].toFloat())
            1 -> aabb(tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat(), tokens[0].toFloat())
            else -> throw IllegalArgumentException()
        }
    }
}