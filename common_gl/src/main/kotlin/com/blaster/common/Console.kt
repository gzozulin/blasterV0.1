package com.blaster.common

class Console(private val timeout: Long = 1000L) {
    enum class Level { FAILURE, INFO, SUCCESS }
    private data class Line(val text: String, val timestamp: Long, val level: Level)
    private val lines = mutableListOf<Line>()

    fun line(text: String, level: Level) {
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

    fun throttle() {
        val current = System.currentTimeMillis()
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            if (current - line.timestamp > timeout) {
                iterator.remove()
            }
        }
    }

    fun render(callback: (index: Int, text: String, level: Level) -> Unit) {
        lines.forEachIndexed { index, line ->
            callback.invoke(index, line.text, line.level)
        }
    }
}