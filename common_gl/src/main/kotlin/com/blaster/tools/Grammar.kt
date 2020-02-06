package com.blaster.tools

import com.blaster.aux.*
import java.util.regex.Pattern

private val SPLIT_LINE = Pattern.compile(":\\s+")
private val SPLIT_RULES = Pattern.compile("\\s+")

private enum class GrammarNodeCnt {
    ONE,            // 1
    NONE_OR_ONE,    // ?
    NONE_OR_MORE,   // *
    ONE_OR_MORE,    // +
}

private data class GrammarNode<T> (
        val label: String,
        val splitRule: SplitRule<T>,
        val children: List<Pair<GrammarNode<T>, GrammarNodeCnt>>?)

typealias SplitRule<T> = (context: T) -> List<T>

class Grammar<T> private constructor() {

    private lateinit var root: GrammarNode<T>

    fun walk(start: T) {
        walkInternal(start, root)
    }

    private fun walkInternal(context: T, node: GrammarNode<T>) {
        val partitions = node.splitRule.invoke(context)
        if (node.children == null) {
            return // terminal
        }
        val iterator = partitions.iterator()
        node.children.forEach {
            check(iterator.hasNext()) { "Not enough partitions to cater for children!" }
            val child = it.first
            val cnt = it.second
            when (cnt) {
                GrammarNodeCnt.NONE_OR_ONE -> TODO()
                GrammarNodeCnt.NONE_OR_MORE -> TODO()
                GrammarNodeCnt.ONE_OR_MORE -> {
                    while (iterator.hasNext()) {
                        walkInternal(iterator.next(), child)
                    }
                }
                GrammarNodeCnt.ONE -> {
                    walkInternal(iterator.next(), child)
                }
            }
        }
    }

    companion object {
        fun <T> compile(grammar: String, splitRules: Map<String, SplitRule<T>>): Grammar<T>
        {
            var rootLabel: String? = null
            val parsed = mutableMapOf<String, List<String>>()
            grammar.lines().forEach {
                if (!it.isBlank()) {
                    val trimmed = it.trim()
                    val split = trimmed.split(SPLIT_LINE)
                    val label = split[0]
                    val rules = split[1]
                    if (rootLabel == null) {
                        rootLabel = label
                    }
                    val rulesSplit = rules.split(SPLIT_RULES)
                    check(!parsed.contains(label))
                    parsed[label] = rulesSplit
                }
            }
            val result = Grammar<T>()
            result.root = parseNode(rootLabel!!, parsed, splitRules)
            return result
        }

        private fun <T> parseNode(label: String,
                                  parsed: Map<String, List<String>>,
                                  splitRules: Map<String, SplitRule<T>>): GrammarNode<T> {
            if (label.filter { !it.isUpperCase() }.count() == 0) {
                return GrammarNode(label, splitRules.getValue(label), null) // terminal node
            }
            val rules = parsed.getValue(label)
            val children = mutableListOf<Pair<GrammarNode<T>, GrammarNodeCnt>>()
            rules.forEach {
                val (child, cnt) = when {
                    it.endsWith("?") -> it.removeSuffix("?") to GrammarNodeCnt.NONE_OR_ONE
                    it.endsWith("*") -> it.removeSuffix("*") to GrammarNodeCnt.NONE_OR_MORE
                    it.endsWith("+") -> it.removeSuffix("+") to GrammarNodeCnt.ONE_OR_MORE
                    else -> it to GrammarNodeCnt.ONE
                }

                children.add(parseNode(child, parsed, splitRules) to cnt)
            }
            return GrammarNode(label, splitRules.getValue(label), children)
        }
    }
}