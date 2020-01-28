package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.GlProgram
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.scene.Mesh
import org.joml.Vector2f

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private lateinit var camera: Camera
private val controller = Controller()
private val wasd = WasdInput(controller)

class SkyboxTechnique {
    private lateinit var program: GlProgram
    private lateinit var cube: Mesh

    fun prepare(shadersLib: ShadersLib, textureLib: TexturesLib, skybox: String) {
        program = shadersLib.loadProgram("shaders/skybox/skybox.vert", "shaders/skybox/skybox.frag")
        val (mesh, aabb) = meshLib.loadMesh("models/cube/cube.obj")
        cube = mesh
    }

    fun skybox() {

    }
}


private val skyboxTechnique = SkyboxTechnique()

private val window = object: LwjglWindow() {
    override fun onCreate(width: Int, height: Int) {
        GlState.apply()
        camera = Camera(width.toFloat() / height.toFloat())
        skyboxTechnique.prepare(shadersLib, texturesLib, "textures/darkskies")
    }

    override fun onTick() {
        GlState.clear()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }
}

fun main() {
    window.show()
}