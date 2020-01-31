package com.blaster.ofc

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.scene.Camera
import com.blaster.scene.Controller
import com.blaster.techniques.SkyboxTechnique
import org.joml.Vector2f

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private lateinit var camera: Camera
private val controller = Controller()
private val wasd = WasdInput(controller)

private val skyboxTechnique = SkyboxTechnique()

private val window = object: LwjglWindow() {
    override fun onCreate() {
        //GlState.apply(width, height)
        //camera = Camera(width.toFloat() / height.toFloat())
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/darkskies")
    }

    override fun onResize(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTick() {
        GlState.clear()
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
    }

    override fun onCursorDelta(delta: Vector2f) {
        wasd.onCursorDelta(delta)
    }
}

fun main() {
    window.show()
}