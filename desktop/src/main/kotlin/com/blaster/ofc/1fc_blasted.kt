package com.blaster.ofc

import com.blaster.scene.SceneDiffer
import com.blaster.scene.SceneReader

// todo: step 1 - use only scene reader in deferred
// todo: step 2 - BlastEd with WYSIWYG

private val example1 = """
    updated; pos 1 1 1; rot 1 1 1 1; scale 1 1 1; custom gold;
        build_1; pos 3 3 3;
            build_1_1; pos 5 5 5;
            build_1_2; pos 5 5 5;
        build_2; pos -3 3 3;
            build_2_1; pos 5 5 5;
        build_3; pos -3 3 3;
    removed; pos 1 2 3;
    camera; pos 4 4 4; target building;
""".trimIndent()

private val example2 = """
    updated; pos 1 1 1; rot 1 1 1 1; scale 1 1 1; custom gold;
        build_1; pos 3 3 3;
            build_1_1; pos 5 5 5;
            build_1_2; pos 5 5 5;
        build_2; pos -3 3 3;
            build_2_1; pos 5 5 5;
        build_3; pos -3 3 3;
        build_4; pos 1 1 1;
    added; pos 4 5 6;
    camera; pos 4 4 4; target building;
        added_to_cam; pos 1 2 3;
""".trimIndent()

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

fun main() {
    val scene1 = sceneReader.load(example1)
    val scene2 = sceneReader.load(example2)
    sceneDiffer.diff(scene1, scene2, {
        parent, marker -> println("$marker was removed from $parent")
    }, {
        marker -> println("$marker was updated")
    }, {
        parent, marker -> println("$marker was added to $parent")
    })
}
