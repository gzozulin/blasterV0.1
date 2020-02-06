package com.blaster.tools

import com.blaster.aux.*
import com.blaster.entity.Marker
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.regex.Pattern

// todo: templates by id
// todo: toLeftOf, toRightOf, toTopOf, toBottomOf, toFrontOf, toBackOf - by aabb (which is always axis aligned)
// todo: probably, also can have matrix directly?
// todo: target as a name
// todo: the rest of the string is custom stuff

private const val START_POS = "pos "
private const val START_QUAT = "quat "
private const val START_EULER = "euler "
private const val START_SCALE = "scale "
private const val START_BOUND = "bound "
private const val START_DIR = "dir "
private const val START_TARGET = "target "

class SceneReader {
    fun load(sceneStream: InputStream): List<Marker> {
        val lines = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
                .readLines().toMutableList()
        return parse(0, lines)
    }

    private fun peek(input: String): Int {
        var count = 0
        while (input[count] == ' ' || input[count] == '\t') {
            count++
        }
        return count
    }

    private fun parse(depth: Int, remainder: MutableList<String>): List<Marker> {
        val uids = hashSetOf<String>()
        val result = mutableListOf<Marker>()
        loop@ while (remainder.isNotEmpty()) {
            if (remainder[0].isBlank() || remainder[0].trim().startsWith("//")) {
                remainder.removeAt(0)
                continue
            }
            val currentDepth = peek(remainder[0])
            when {
                currentDepth == depth -> result.add(parseMarker(remainder.removeAt(0), uids))
                currentDepth > depth -> result.last().children.addAll(parse(currentDepth, remainder))
                currentDepth < depth -> break@loop
            }
        }
        return result
    }

    private fun parseMarker(marker: String, uids: MutableSet<String>): Marker {
        val tokens = marker.trim().split(Pattern.compile(";\\s*")).dropLast(1)
        val uid: String = tokens[0]
        check(uids.add(uid)) { "Non unique uid: $uid" }
        var pos: vec3? = null
        var quat: quat? = null
        var euler: euler3? = null
        var scale: vec3? = null
        var bound: Float? = null
        var dir: vec3? = null
        var target: vec3? = null
        val custom = mutableListOf<String>()
        tokens.forEachIndexed { index, token ->
            val trimmed = token.trim()
            when {
                trimmed.startsWith(START_POS)    -> pos      = trimmed.removePrefix(START_POS).toVec3()
                trimmed.startsWith(START_QUAT)   -> quat     = trimmed.removePrefix(START_QUAT).toQuat()
                trimmed.startsWith(START_EULER)  -> euler    = trimmed.removePrefix(START_EULER).toVec3()
                trimmed.startsWith(START_SCALE)  -> scale    = trimmed.removePrefix(START_SCALE).toVec3()
                trimmed.startsWith(START_BOUND)  -> bound    = trimmed.removePrefix(START_BOUND).toFloat()
                trimmed.startsWith(START_DIR)    -> dir      = trimmed.removePrefix(START_DIR).toVec3()
                trimmed.startsWith(START_TARGET) -> target   = trimmed.removePrefix(START_TARGET).toVec3()
                else -> if (index != 0) custom.add(token)
            }
        }
        return Marker(uid, pos ?: vec3(), euler, quat, scale, bound, dir, target, custom, mutableListOf())
    }
}