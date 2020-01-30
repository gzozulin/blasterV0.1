package com.blaster.editor

import com.blaster.common.Console
import com.blaster.entity.Marker
import java.lang.IllegalStateException

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
                val uidFoundParent = found.parent?.uid
                val uidPrevParent = prev.parent?.uid
                if (uidFoundParent != uidPrevParent) {
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

class MultiListener(private val console: Console? = null, private val listeners: Map<String, SceneDiffer.Listener>)
    : SceneDiffer.Listener() {
    override fun onRemove(marker: Marker) {
        getListener(marker).onRemove(marker)
        console?.info("Marker removed: ${marker.uid}")
    }

    override fun onUpdate(marker: Marker) {
        getListener(marker).onUpdate(marker)
        console?.info("Marker updated: ${marker.uid}")
    }

    override fun onAdd(marker: Marker) {
        getListener(marker).onAdd(marker)
        console?.info("Marker added: ${marker.uid}")
    }

    override fun onParent(marker: Marker, parent: Marker?) {
        getListener(marker).onParent(marker, parent)
        console?.info("Marker ${marker.uid} attached to ${parent?.uid}")
    }

    private fun getListener(marker: Marker): SceneDiffer.Listener {
        listeners.forEach {
            if (marker.uid.startsWith(it.key)) {
                return it.value
            }
        }
        throw IllegalStateException("Listener is not registered! ${marker.uid}")
    }
}

