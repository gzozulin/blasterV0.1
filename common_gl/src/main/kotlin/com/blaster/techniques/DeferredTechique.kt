package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.common.mat3
import com.blaster.common.mat4
import com.blaster.common.vec3
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.entity.Light
import com.blaster.entity.Material
import com.blaster.scene.Mesh
import org.joml.Matrix4f

private val backend = GlLocator.locate()

const val MAX_LIGHTS = 128

// todo: transparency from material
class DeferredTechnique {
    private lateinit var programGeomPass: GlProgram
    private lateinit var programLightPass: GlProgram

    private lateinit var quadMesh: Mesh

    private lateinit var framebuffer: GlFrameBuffer

    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture

    private lateinit var matAmbShineStorage: GlTexture // ambient + shine
    private lateinit var matDiffTranspStorage: GlTexture // diffuse + transparency
    private lateinit var matSpecularStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    data class LightData(val light: Light, val modelM: Matrix4f)
    private val lightVectorBuf = vec3()
    private val lights = mutableListOf<LightData>()

    fun prepare(shadersLib: ShadersLib, width: Int, height: Int) {
        programGeomPass = shadersLib.loadProgram(
                "shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shadersLib.loadProgram(
                "shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        quadMesh = Mesh.rect()
        positionStorage = GlTexture(
                unit = 0,
                width = width, height = height, internalFormat = backend.GL_RGBA16F,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        normalStorage = GlTexture(
                unit = 1,
                width = width, height = height, internalFormat = backend.GL_RGB16F,
                pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        diffuseStorage = GlTexture(
                unit = 2,
                width = width, height = height, internalFormat = backend.GL_RGBA,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_UNSIGNED_BYTE)
        matAmbShineStorage = GlTexture(
                unit = 3,
                width = width, height = height, internalFormat = backend.GL_RGBA16F,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matDiffTranspStorage = GlTexture(
                unit = 4,
                width = width, height = height, internalFormat = backend.GL_RGBA16F,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        matSpecularStorage = GlTexture(
                unit = 5,
                width = width, height = height, internalFormat = backend.GL_RGB16F,
                pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        depthBuffer = GlRenderBuffer(width = width, height = height)
        framebuffer = GlFrameBuffer()
        glBind(framebuffer) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT3, matAmbShineStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT4, matDiffTranspStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT5, matSpecularStorage)
            framebuffer.setOutputs(intArrayOf(
                    backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2,
                    backend.GL_COLOR_ATTACHMENT3, backend.GL_COLOR_ATTACHMENT4, backend.GL_COLOR_ATTACHMENT5
            ))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glBind(programLightPass) {
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_POSITION, positionStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL, normalStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuseStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_AMB_SHINE, matAmbShineStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_DIFF_TRANSP, matDiffTranspStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_MAT_SPECULAR, matSpecularStorage)
        }
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(programGeomPass, framebuffer)) {
            programGeomPass.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            programGeomPass.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            draw.invoke()
        }
        GlState.drawTransparent {
            glBind(listOf(programLightPass, quadMesh, positionStorage, normalStorage, diffuseStorage, depthBuffer,
                    matAmbShineStorage, matDiffTranspStorage, matSpecularStorage)) {
                programLightPass.setUniform(GlUniform.UNIFORM_EYE, camera.position)
                quadMesh.draw()
            }
        }
    }

    fun light(light: Light, modelM: Matrix4f) {
        lights.add(LightData(light, modelM))
        updateLightsUniforms()
    }

    fun setLights(data: List<LightData>) {
        lights.clear()
        lights.addAll(data)
        updateLightsUniforms()
    }

    private fun updateLightsUniforms() {
        check(lights.size <= MAX_LIGHTS) { "More lights than defined in shader!" }
        val pointLights = mutableListOf<LightData>()
        val dirLights = mutableListOf<LightData>()
        lights.forEach {
            if (it.light.point) {
                pointLights.add(it)
            } else {
                dirLights.add(it)
            }
        }
        glBind(programLightPass) {
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLights.size)
            programLightPass.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLights.size)
            pointLights.forEachIndexed { index, lightData ->
                lightData.modelM.getColumn(3, lightVectorBuf)
                programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, index, lightVectorBuf)
                programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, index, lightData.light.intensity)
            }
            dirLights.forEachIndexed { index, lightData ->
                lightData.modelM.getRow(2, lightVectorBuf) // transpose
                lightVectorBuf.negate() // -Z
                programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, index, lightVectorBuf)
                programLightPass.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, index, lightData.light.intensity)
            }
        }
    }

    fun instance(mesh: Mesh, modelM: Matrix4f, diffuse: GlTexture, material: Material) {
        glBind(listOf(mesh, diffuse)) {
            programGeomPass.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_AMBIENT, material.ambient)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_DIFFUSE, material.diffuse)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SPECULAR, material.specular)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_SHINE, material.shine)
            programGeomPass.setUniform(GlUniform.UNIFORM_MAT_TRANSP, material.transparency)
            programGeomPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            mesh.draw()
        }
    }
}