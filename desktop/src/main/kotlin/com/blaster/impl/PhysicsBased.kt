package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.entity.Camera
import com.blaster.entity.Controller
import com.blaster.entity.Light
import com.blaster.gl.GlMesh
import com.blaster.gl.GlProgram
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import org.joml.Matrix4f
import org.joml.Vector2f

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val camera = Camera()
private val controller = Controller()
private val wasdInput = WasdInput(controller)

class PbrMaterial

class PbrTechnique {
    private lateinit var program: GlProgram

    fun create() {
        program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")
    }

    fun draw(camera: Camera, meshes: () -> Unit, lights: () -> Unit) {

    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {

    }

    fun light(light: Light) {

    }
}

private val pbrTechnique = PbrTechnique()

private val window = object : LwjglWindow() {
    override fun onCreate() {
        pbrTechnique.create()
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height)
        camera.setPerspective(width.toFloat() / height.toFloat())
    }

    override fun onTick() {
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.clear()
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasdInput.onCursorDelta(delta)
    }

    override fun keyPressed(key: Int) {
        wasdInput.keyPressed(key)
    }

    override fun keyReleased(key: Int) {
        wasdInput.keyReleased(key)
    }
}

fun main() {
    window.show()
}