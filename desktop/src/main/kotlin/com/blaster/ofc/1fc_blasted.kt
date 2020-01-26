package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.ModelsLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.mat4
import com.blaster.common.vec3
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.*
import com.blaster.techniques.DeferredTechnique
import org.joml.Vector2f

// todo: step 1 - use only scene reader
// todo: step 2 - BlastEd with WYSIWYG

private val scene = """
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

private val sceneReader = SceneReader()
private val sceneDiffer = SceneDiffer()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val modelsLib = ModelsLib(assetStream, texturesLib)

private val deferredTechnique = DeferredTechnique()

private lateinit var camera: Camera
private val controller = Controller(velocity = 0.05f)
private val wasd = WasdInput(controller)

private val listener = object : SceneDiffer.Listener() {

}

private lateinit var model: Model

private val window = object : LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        controller.position.set(vec3(0.5f, 3f, 3f))
        model = modelsLib.loadModel("models/house/low.obj", "models/house/house_diffuse.png")
        deferredTechnique.prepare(shadersLib, width, height)
        camera = Camera(width.toFloat() / height.toFloat())
        sceneDiffer.diff(nextMarkers = sceneReader.load(scene), listener = listener)
        deferredTechnique.light(Light.SUNLIGHT)
    }

    override fun onDraw() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
        deferredTechnique.draw(camera) {
            deferredTechnique.instance(model.mesh, mat4(), model.diffuse, Material.CONCRETE)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasd.keyPressed(key)
    }

    override fun keyReleased(key: Int) {
        wasd.keyReleased(key)
    }
}

fun main() {
    window.show()
}
