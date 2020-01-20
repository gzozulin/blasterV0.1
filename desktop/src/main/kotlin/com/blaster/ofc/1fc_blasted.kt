package com.blaster.ofc

import com.blaster.common.quat
import com.blaster.common.vec3
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

// todo: BlastEd

// todo: create when needed
// todo: remove when needed
// todo: update when needed

private val example = """
    building; pos 1 1 1; rot 1 1 1 1; scale 1 1 1; custom gold;
        build_1; pos 3 3 3;
            build_1_1; pos 5 5 5;
            build_1_2; pos 5 5 5;
        build_2; pos -3 3 3;
            build_2_1; pos 5 5 5;
        build_3; pos -3 3 3;
    camera; pos 4 4 4; target building;
""".trimIndent()

// todo: toLeftOf, toRightOf, toBottomOf, toTopOf, toFrontOf, toBackOf instead of positions, based on AABB
// todo: probably, also can have matrix directly
data class Placeholer(
        val uid: String,
        val pos: vec3, val rotation: quat? = null, val scale: vec3? = null,
        val target: String? = null,
        val custom: String? = null,
        val children: MutableList<Placeholer> = mutableListOf())

class SceneReader(
        private val sceneStream: InputStream, private val reloadFrequency: Long = 1000) {

    private var scene: Placeholer = Placeholer("scene", vec3())

    private var last = 0L

    // todo: streaming/comparing should be a separate feature with SceneDiffer
    fun tick() {
        val current = System.currentTimeMillis()
        if (current - last > reloadFrequency) {
            reload()
            last = current
        }
    }

    private fun reload() {
        val bufferedReader = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
        val new = parse(0, bufferedReader.readLines().toMutableList())
        //diff(scene, new)
        //scene = new
        return
    }

    private fun peek(input: String): Int {
        var count = 0
        while (input[count] == ' ') {
            count++
        }
        return count
    }

    private fun parse(depth: Int, remainder: MutableList<String>): List<Placeholer> {
        val result = mutableListOf<Placeholer>()
        loop@ while (remainder.isNotEmpty()) {
            val currentDepth = peek(remainder[0])
            when {
                currentDepth == depth -> result.add(parsePlaceholder(remainder.removeAt(0)))
                currentDepth > depth -> result.last().children.addAll(parse(currentDepth, remainder))
                currentDepth < depth -> break@loop
            }
        }
        return result
    }

    private fun parsePlaceholder(placeholder: String): Placeholer {
        val tokens = placeholder.trim().split(Pattern.compile(";\\s*"))
        val uid: String = tokens[0]
        var pos: vec3? = null
        var rot: quat? = null
        var scale: vec3? = null
        var target: String? = null
        var custom: String? = null
        tokens.forEach {
            when {
                it.startsWith("pos") -> pos = parseVec3(it.removePrefix("pos").trim())
                it.startsWith("rot") -> rot = parseQuat(it.removePrefix("rot").trim())
                it.startsWith("scale") -> scale = parseVec3(it.removePrefix("scale").trim())
                it.startsWith("target") -> target = it.removePrefix("target").trim()
                it.startsWith("custom") -> custom = it.removePrefix("custom").trim()
            }
        }
        return Placeholer(uid, pos!!, rot, scale, target, custom, mutableListOf())
    }

    private fun parseVec3(value: String): vec3 {
        val tokens = value.split(Pattern.compile("\\s+"))
        check(tokens.size == 3)
        return vec3(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat())
    }

    private fun parseQuat(value: String): quat {
        val tokens = value.split(Pattern.compile("\\s+"))
        check(tokens.size == 4)
        return quat(tokens[0].toFloat(), tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
    }

    private fun diff(current: Placeholer, new: Placeholer) {
        // onAdd
        // onRemove
        // onUpdate
    }
}

private val sceneReader = SceneReader(example.byteInputStream(StandardCharsets.UTF_8))

fun main() {
    sceneReader.tick()
}
