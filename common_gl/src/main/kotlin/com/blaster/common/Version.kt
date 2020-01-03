package com.blaster.common

data class Version(private var version: Long = 0L, private var last: Long = Long.MAX_VALUE) {

    fun increment() {
        version++
    }

    fun check(): Boolean {
        return if (version != last) {
            version = last
            true
        }
        else {
            false
        }
    }
}