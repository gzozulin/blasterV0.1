package com.blaster.scene

class SceneDiffer {
    fun diff(prevMarkers: List<Marker> = listOf(), nextMarkers: List<Marker>,
             onRemove: (parent: Marker?, marker: Marker) -> Unit = { _, _ -> },
             onUpdate: (marker: Marker) -> Unit = { _ -> },
             onAdd: (parent: Marker?, next: Marker) -> Unit) {
        diffInternal(null, prevMarkers, nextMarkers, onRemove, onUpdate, onAdd)
    }

    private fun diffInternal(parent: Marker? = null,
             prevMarkers: List<Marker>, nextMarkers: List<Marker>,
             onRemove: (parent: Marker?, marker: Marker) -> Unit,
             onUpdate: (marker: Marker) -> Unit,
             onAdd: (parent: Marker?, next: Marker) -> Unit) {
        val added = mutableListOf<Marker>()
        val removed = mutableListOf<Marker>()
        val updated = mutableListOf<Marker>()
        nextMarkers.forEach{ next ->
            if (prevMarkers.none { prev -> prev.uid == next.uid }) {
                added.add(next)
            }
        }
        prevMarkers.forEach{ prev ->
            val filtered = nextMarkers.filter { next -> prev.uid == next.uid }
            if (filtered.isEmpty()) {
                removed.add(prev)
            } else {
                check(filtered.size == 1) { "Non unique uid?!" }
                val next = filtered.first()
                if (next != prev) {
                    updated.add(prev)
                    diffInternal(prev, prev.children, next.children, onRemove, onUpdate, onAdd)
                }
            }
        }
        removed.forEach { onRemove.invoke(parent, it) }
        updated.forEach(onUpdate)
        added.forEach { onAdd.invoke(parent, it) }
    }
}