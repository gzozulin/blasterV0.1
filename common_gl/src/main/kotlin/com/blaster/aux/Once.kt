package com.blaster.aux

class Once {
    private var flag = true

    fun check(): Boolean =
            if (flag) {
                flag = false
                true
            } else {
                false
            }
}