package com.blaster.ofc

import com.blaster.common.quat
import com.blaster.common.vec3
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

// todo: BlastEd

// todo: create when needed
// todo: remove when needed
// todo: update when needed

private val example = """
    building; pos 1 1 1; rot 1 1 1 1; scale 1 1 1;
        right_top_light; pos 3 3 3;
            right_one_more; pos 5 5 5;
        left_top_light; pos -3 3 3;
    camera; pos 4 4 4; target building;
""".trimIndent()

// todo: probably, also can have matrix directly
data class Placeholer(
        val uid: String,
        val pos: vec3, val rotation: quat?, val scale: vec3?,
        val target: String?,
        val children: MutableList<Placeholer>)

class SceneReader(
        private val sceneStream: InputStream, private val reloadFrequency: Long = 1000) {

    private val scene = mutableListOf<Placeholer>()

    private var last = 0L

    fun tick() {
        val current = System.currentTimeMillis()
        if (current - last > reloadFrequency) {
            reload()
            last = current
        }
    }

    private fun reload() {
        val new = parse()
        compare(scene, new)
        scene.clear()
        scene.addAll(new)
    }

    private fun parse(): List<Placeholer> {
        val result = mutableListOf<Placeholer>()


        val stack = Stack<Placeholer>()
        stack.add(parsePlaceholder("scene; pos 0 0 0; rot 0 0 0 0; scale 1 1 1;"))

        val bufferedReader = BufferedReader(InputStreamReader(sceneStream, Charset.defaultCharset()))
        bufferedReader.use {
            var line = bufferedReader.readLine()
            while (line != null) {

                val depthToLine = removeDepth(line)

                when {
                    depthToLine.first > stack.size -> {
                        stack.peek().children.add(parsePlaceholder(depthToLine.second))
                    }
                    depthToLine.first < stack.size -> {
                        result.add(stack.pop())
                        stack.add(parsePlaceholder(depthToLine.second))
                    }
                    else -> {
                        stack.add(parsePlaceholder(depthToLine.second))
                    }
                }


                line = bufferedReader.readLine()
            }
        }
        return result
    }

    private fun parsePlaceholder(placeholder: String): Placeholer {
        val tokens = placeholder.split(Pattern.compile(";\\s*"))
        val uid: String = tokens[0]
        var pos: vec3? = null
        var rot: quat? = null
        var scale: vec3? = null
        var target: String? = null
        tokens.forEach {
            when {
                it.startsWith("pos") -> {
                    pos = parseVec3(it.removePrefix("pos").trim())
                }
                it.startsWith("rot") -> {
                    rot = parseQuat(it.removePrefix("rot").trim())
                }
                it.startsWith("scale") -> {
                    scale = parseVec3(it.removePrefix("scale").trim())
                }
                it.startsWith("target") -> {
                    target = it.removePrefix("target").trim()
                }
            }
        }
        return Placeholer(uid, pos!!, rot, scale, target, mutableListOf())
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

    private fun compare(current: List<Placeholer>, new: List<Placeholer>) {

    }

    private fun removeDepth(input: String): Pair<Int, String> {
        var current = input
        var depth = 0
        while (current.startsWith(' ')) {
            current = current.removePrefix(" ")
            depth ++
        }

        return depth / 4 to current
    }
}

private val sceneReader = SceneReader(example.byteInputStream(StandardCharsets.UTF_8))

fun main() {
    sceneReader.tick()
}
