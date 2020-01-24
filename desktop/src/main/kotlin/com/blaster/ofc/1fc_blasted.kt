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

// todo: scale aabb to fit, but maintain ratios

// todo: create when needed
// todo: remove when needed
// todo: update when needed

// todo: toLeftOf, toRightOf, toTopOf, toBottomOf, toFrontOf, toBackOf - by aabb (which is always axis aligned)

// todo: eulers in degrees for rotation

// todo: probably, also can have matrix directly

private val example1 = """
    updated; pos 1 1 1; rot 1 1 1 1; scale 1 1 1; custom gold;
        build_1; pos 3 3 3;
            build_1_1; pos 5 5 5;
            build_1_2; pos 5 5 5;
        build_2; pos -3 3 3;
            build_2_1; pos 5 5 5;
        build_3; pos -3 3 3;
    removed; pos 1 2 3;
    camera; pos 4 4 4; target building;
""".trimIndent()

private val example2 = """
    updated; pos 1 1 1; rot 1 1 1 1; scale 1 1 1; custom gold;
        build_1; pos 3 3 3;
            build_1_1; pos 5 5 5;
            build_1_2; pos 5 5 5;
        build_2; pos -3 3 3;
            build_2_1; pos 5 5 5;
        build_3; pos -3 3 3;
        build_4; pos 1 1 1;
    added; pos 4 5 6;
    camera; pos 4 4 4; target building;
""".trimIndent()

data class Placeholder(
        val uid: String,
        val pos: vec3, val rotation: quat? = null, val scale: vec3? = null,
        val target: String? = null,
        val custom: String? = null,
        val children: MutableList<Placeholder> = mutableListOf())

class SceneReader {
    fun load(sceneStream: InputStream): List<Placeholder> {
        val bufferedReader = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
        return parse(0, bufferedReader.readLines().toMutableList())
    }

    private fun peek(input: String): Int {
        var count = 0
        while (input[count] == ' ') {
            count++
        }
        return count
    }

    private fun parse(depth: Int, remainder: MutableList<String>): List<Placeholder> {
        val result = mutableListOf<Placeholder>()
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

    private fun parsePlaceholder(placeholder: String): Placeholder {
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
        return Placeholder(uid, pos!!, rot, scale, target, custom, mutableListOf())
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
}

class SceneDiffer {
    fun diff(current: List<Placeholder>, new: List<Placeholder>,
             onRemove: (placeholder: Placeholder) -> Unit,
             onUpdate: (placeholder: Placeholder) -> Unit,
             onAdd: (placeholder: Placeholder) -> Unit) {
        val added = mutableListOf<Placeholder>()
        val removed = mutableListOf<Placeholder>()
        val updated = mutableListOf<Placeholder>()
        new.forEach{ placeholder ->
            if (current.none { it.uid == placeholder.uid }) {
                added.add(placeholder)
            }
        }
        current.forEach{ placeholder ->
            val filtered = new.filter { placeholder.uid == it.uid }
            if (filtered.isEmpty()) {
                removed.add(placeholder)
            } else {
                check(filtered.size == 1) { "Non unique uid?!" }
                if (filtered.first() != placeholder) {
                    updated.add(filtered.first())
                }
            }
        }
        removed.forEach(onRemove)
        updated.forEach(onUpdate)
        added.forEach(onAdd)
    }
}

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

fun main() {
    val scene1 = sceneReader.load(example1.byteInputStream(StandardCharsets.UTF_8))
    val scene2 = sceneReader.load(example2.byteInputStream(StandardCharsets.UTF_8))
    sceneDiffer.diff(scene1, scene2, ::println, ::println, ::println)
}
