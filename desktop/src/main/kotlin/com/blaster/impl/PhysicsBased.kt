package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.auxiliary.*
import com.blaster.entity.*
import com.blaster.gl.GlMesh
import com.blaster.gl.GlState
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.PbrTechnique
import com.blaster.techniques.SkyboxTechnique
import org.lwjgl.glfw.GLFW

private val vecUp = vec3().up()
private val colorWhite = color().white()

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val clearColor = color(0f, 0.5f, 0.5f)

private val camera = Camera()
private val controller = Controller(velocity = 0.05f, position = vec3(0f, 2.5f, 4f))
private val wasdInput = WasdInput(controller)

private val masternode = Node<GlMesh>()

private val light = Light(vec3(25f), true)
private val lightNode1 = Node(payload = light).setPosition(vec3(3f))
private val lightNode2 = Node(payload = light).setPosition(vec3(-3f, 3f, -3f))

private lateinit var model: GlMesh
private lateinit var pbrMaterial: PbrMaterial
private lateinit var meshNode: Node<GlMesh>

private val immediateTechnique = ImmediateTechnique()
private val skyboxTechnique = SkyboxTechnique()

private var mouseControl = false
private var showImmediate = false

private val pbrTechnique = PbrTechnique()

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        pbrTechnique.create(shadersLib)
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/miramar")
        val (mesh, aabb) = meshLib.loadMesh("models/lantern/lantern.obj")
        model = mesh
        pbrMaterial = texturesLib.loadPbr("models/lantern", "jpg")
        meshNode = Node(parent = masternode, payload = model).setScale(aabb.scaleTo(5f))
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height, clearColor)
        camera.setPerspective(width.toFloat() / height.toFloat())
        immediateTechnique.resize(camera)
    }

    override fun onTick() {
        GlState.clear()
        masternode.rotate(vecUp, 0.01f)
        controller.apply { position, direction ->
            camera.setPosition(position)
            camera.lookAlong(direction)
        }
        GlState.drawWithNoCulling {
            skyboxTechnique.skybox(camera)
        }
        pbrTechnique.draw(
                camera = camera,
                lights = {
                    pbrTechnique.light(lightNode1.payload(), lightNode1.calculateM())
                    pbrTechnique.light(lightNode2.payload(), lightNode2.calculateM())
                },
                meshes = {
                    pbrTechnique.instance(meshNode.payload(), meshNode.calculateM(), pbrMaterial)
                })
        if (showImmediate) {
            immediateTechnique.marker(camera, lightNode1.calculateM(), colorWhite)
            immediateTechnique.marker(camera, lightNode2.calculateM(), colorWhite)
        }
    }

    override fun onCursorDelta(delta: vec2) {
        if (mouseControl) {
            wasdInput.onCursorDelta(delta)
        }
    }

    override fun mouseBtnPressed(btn: Int) {
        mouseControl = true
    }

    override fun mouseBtnReleased(btn: Int) {
        mouseControl = false
    }

    override fun keyPressed(key: Int) {
        wasdInput.keyPressed(key)
        if (key == GLFW.GLFW_KEY_SPACE) {
            showImmediate = !showImmediate
        }
    }

    override fun keyReleased(key: Int) {
        wasdInput.keyReleased(key)
    }
}

fun main() {
    window.show()
}