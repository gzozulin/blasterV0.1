package com.blaster.impl

import com.blaster.assets.AssetStream
import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.aux.vec3
import com.blaster.entity.*
import com.blaster.gl.*
import com.blaster.platform.LwjglWindow
import com.blaster.platform.WasdInput
import com.blaster.techniques.MAX_LIGHTS
import org.joml.Matrix4f
import org.joml.Vector2f

private val assetStream = AssetStream()
private val shadersLib = ShadersLib(assetStream)
private val texturesLib = TexturesLib(assetStream)
private val meshLib = MeshLib(assetStream)

private val camera = Camera()
private val controller = Controller(velocity = 1f)
private val wasdInput = WasdInput(controller)

private val light = Light(vec3(22f), true)
private val lightNode = Node(payload = light).setPosition(vec3(10f))

private lateinit var mandalorian: GlMesh
private lateinit var mandalorianMaterial: PbrMaterial
private lateinit var mandalorianNode: Node<GlMesh>

class PbrTechnique {
    private lateinit var program: GlProgram

    fun create() {
        program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(listOf(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            //program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO, material.albedo)
            //program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL, material.normal)
            //program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC, material.metallic)
            //program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS, material.roughness)
            //program.setTexture(GlUniform.UNIFORM_TEXTURE_AO, material.ao)
            mesh.draw()
        }
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
}

private val pbrTechnique = PbrTechnique()

private val window = object : LwjglWindow() {
    override fun onCreate() {
        pbrTechnique.create()
        val (mesh, aabb) = meshLib.loadMesh("models/mandalorian/mandalorian.obj")
        mandalorian = mesh
        mandalorianMaterial = texturesLib.loadPbr("models/mandalorian")
        mandalorianNode = Node(payload = mandalorian)
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
        pbrTechnique.draw(camera) {
            pbrTechnique.light(lightNode.payload(), lightNode.calculateM())
            pbrTechnique.instance(mandalorianNode.payload(), mandalorianNode.calculateM(), mandalorianMaterial)
        }
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