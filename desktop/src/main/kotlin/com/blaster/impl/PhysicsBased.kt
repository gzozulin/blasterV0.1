package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.aux.*
import com.blaster.entity.*
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.techniques.ImmediateTechnique
import com.blaster.techniques.MAX_LIGHTS
import com.blaster.techniques.SkyboxTechnique
import org.joml.Matrix4f
import org.joml.Vector2f

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

private val light = Light(vec3(25f), true)
private val lightMasternode = Node<Light>()
private val lightNode1 = Node(parent = lightMasternode, payload = light).setPosition(vec3(3f))
private val lightNode2 = Node(parent = lightMasternode, payload = light).setPosition(vec3(-3f, 3f, -3f))

private lateinit var mandalorian: GlMesh
private lateinit var mandalorianMaterial: PbrMaterial
private lateinit var mandalorianNode: Node<GlMesh>

private val immediateTechnique = ImmediateTechnique()
private val skyboxTechnique = SkyboxTechnique()

private var mouseControl = false

class PbrTechnique {
    private lateinit var program: GlProgram

    fun create() {
        program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, lights: () -> Unit, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M,    camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M,    camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE,       camera.position)
            lights.invoke()
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        if (light.point) {
            modelM.getColumn(3, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, pointLightCnt, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, pointLightCnt, light.intensity)
            pointLightCnt++
        } else {
            TODO()
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(listOf(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M,           modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO,    material.albedo)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL,    material.normal)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC,  material.metallic)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS, material.roughness)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_AO,        material.ao)
            mesh.draw()
        }
    }
}

private val pbrTechnique = PbrTechnique()

private val window = object : LwjglWindow(isHoldingCursor = false) {
    override fun onCreate() {
        pbrTechnique.create()
        skyboxTechnique.create(shadersLib, texturesLib, meshLib, "textures/snowy")
        val (mesh, aabb) = meshLib.loadMesh("models/mandalorian/mandalorian.obj")
        mandalorian = mesh
        mandalorianMaterial = texturesLib.loadPbr("models/mandalorian", "png",
                albedo = "models/mandalorian/albedo.png")
        mandalorianNode = Node(payload = mandalorian).setScale(aabb.scaleTo(5f))
    }

    override fun onResize(width: Int, height: Int) {
        GlState.apply(width, height, clearColor)
        camera.setPerspective(width.toFloat() / height.toFloat())
        immediateTechnique.resize(camera)
    }

    override fun onTick() {
        GlState.clear()
        lightMasternode.rotate(vecUp, 0.01f)
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
                    pbrTechnique.instance(mandalorianNode.payload(), mandalorianNode.calculateM(), mandalorianMaterial)
                })
        immediateTechnique.marker(camera, lightNode1.calculateM(), colorWhite)
        immediateTechnique.marker(camera, lightNode2.calculateM(), colorWhite)
    }

    override fun onCursorDelta(delta: Vector2f) {
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
    }

    override fun keyReleased(key: Int) {
        wasdInput.keyReleased(key)
    }
}

fun main() {
    window.show()
}