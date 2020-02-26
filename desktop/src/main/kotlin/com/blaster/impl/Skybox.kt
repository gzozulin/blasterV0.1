package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.auxiliary.up
import com.blaster.auxiliary.vec3
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.entity.Camera
import com.blaster.entity.Controller
import com.blaster.techniques.SkyboxTechnique
import org.joml.Vector2f

private val upVec = vec3().up()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val camera = Camera()

private val skyboxTechnique = SkyboxTechnique()

private val window = object: LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/darkskies")
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height)
        camera.setPerspective(width, height)
    }

    override fun onTick() {
        GlState.clear()
        camera.rotate(0.005f, upVec)
        drawSkybox()
    }

    private fun drawSkybox() {
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
    }
}

fun main() {
    window.show()
}