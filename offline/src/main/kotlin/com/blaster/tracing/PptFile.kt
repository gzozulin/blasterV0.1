package com.blaster.tracing

import com.blaster.scene.Vec3
import java.io.File

class PptFile(width: Int, height: Int, regionsCnt: Int) {
    private val header = "P3\n$width $height\n255\n"

    private val regions = Array(regionsCnt) { "" }

    fun appendRegion(index: Int, vec: Vec3) {
        regions[index] += "${(255.99f * vec.x).toInt()} ${(255.99f * vec.y).toInt()} ${(255.99f * vec.z).toInt()}\n"
    }

    fun flush() {
        val ppt = File("out.ppm")
        ppt.writeText(header)
        for (region in regions.reversed()) {
            ppt.appendText(region)
        }
    }
}