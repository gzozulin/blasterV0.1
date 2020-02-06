package com.blaster.tools

import com.blaster.aux.parseColor
import org.joml.Vector2f
import org.joml.Vector3f

private val COLOR_FAILURE = parseColor("ffabab")
private val COLOR_INFO = parseColor("6eb5ff")
private val COLOR_SUCCESS = parseColor("9ee09e")

private const val TEXT_SCALE = 0.025f

private const val START_X = -1 + TEXT_SCALE / 2f
private const val START_Y = 1 - TEXT_SCALE

private val POSITION = Vector2f()

// todo: stick something by id and remove it after

class Console(private val timeout: Long = 1000L) {
    enum class Level { FAILURE, INFO, SUCCESS }
    private data class Line(val text: String, val timestamp: Long, val level: Level)
    private val lines = mutableListOf<Line>()

    private fun line(text: String, level: Level) {
        lines.add(Line(text, System.currentTimeMillis(), level))
    }

    fun failure(text: String) {
        line(text, Level.FAILURE)
    }

    fun info(text: String) {
        line(text, Level.INFO)
    }

    fun success(text: String) {
        line(text, Level.SUCCESS)
    }

    fun tick() {
        val current = System.currentTimeMillis()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (current - line.timestamp > timeout) {
                iterator.remove()
            }
        }
    }

    fun render(callback: (position: Vector2f, text: String, color: Vector3f, scale: Float) -> Unit) {
        lines.forEachIndexed { index, line ->
            val color = when (line.level) {
                Level.FAILURE -> COLOR_FAILURE
                Level.INFO -> COLOR_INFO
                Level.SUCCESS -> COLOR_SUCCESS
            }
            POSITION.set(START_X, START_Y - TEXT_SCALE * index * 2f)
            callback.invoke(POSITION, line.text, color, TEXT_SCALE)
        }
    }
}