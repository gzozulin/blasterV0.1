package com.blaster.scene

class SceneDiffer {
    open class Listener {
        open fun onRemove(marker: Marker) {}
        open fun onAdd(marker: Marker) {}
        open fun onUpdate(marker: Marker) {}
        open fun onParent(marker: Marker, parent: Marker?) {}
    }

    fun diff(prevMarkers: List<Marker> = listOf(), nextMarkers: List<Marker>, listener: Listener) {
        diffInternal(prevMarkers, nextMarkers, listener)
    }

    private data class ParentToChild(val parent: Marker?, val child: Marker)

    private fun enumerate(parent: Marker?, markers: List<Marker>, parentToChild: MutableList<ParentToChild>) {
        markers.forEach {
            parentToChild.add(ParentToChild(parent, it))
            enumerate(it, it.children, parentToChild)
        }
    }

    private fun diffInternal(prevMarkers: List<Marker>, nextMarkers: List<Marker>, listener: Listener) {
        val parentToChildPrev = mutableListOf<ParentToChild>()
        enumerate(null, prevMarkers, parentToChildPrev)
        val parentToChildNext = mutableListOf<ParentToChild>()
        enumerate(null, nextMarkers, parentToChildNext)
        parentToChildPrev.forEach { prev ->
            val found = parentToChildNext.firstOrNull { prev.child.uid == it.child.uid }
            if (found == null) {
                listener.onRemove(prev.child)
            } else {
                if (found.child != prev.child) {
                    listener.onUpdate(found.child)
                }
                if (found.parent != prev.parent) {
                    listener.onParent(found.child, found.parent)
                }
            }
        }
        parentToChildNext.forEach { next ->
            val found = parentToChildPrev.firstOrNull { next.child.uid == it.child.uid }
            if (found == null) {
                listener.onAdd(next.child)
                listener.onParent(next.child, next.parent)
            }
        }
    }
}