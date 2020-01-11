package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.common.randomVector3f
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Model
import org.joml.Matrix4f
import org.joml.Vector3f

private val backend = GlLocator.locate()

class DeferredTechnique {
    private lateinit var programGeomPass: GlProgram
    private lateinit var programLightPass: GlProgram

    private lateinit var quadMesh: GlMesh

    private lateinit var framebuffer: GlFrameBuffer
    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    fun prepare(shadersLib: ShadersLib, width: Int, height: Int) {
        programGeomPass = shadersLib.loadProgram(
                "shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shadersLib.loadProgram(
                "shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        quadMesh = GlMesh.rect()
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
        depthBuffer = GlRenderBuffer(width = width, height = height)
        framebuffer = GlFrameBuffer()
        glBind(listOf(framebuffer)) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setOutputs(intArrayOf(backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glBind(programLightPass) {
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_POSITION, positionStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL, normalStorage)
            programLightPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuseStorage)
            for (i in 0..15) {
                val randomPos = randomVector3f(Vector3f(-5f), Vector3f(5f))
                val randomColor = randomVector3f(Vector3f(), Vector3f(1f))
                programLightPass.setUniform(GlUniform.uniformLightPosition(i), randomPos)
                programLightPass.setUniform(GlUniform.uniformLightColor(i), randomColor)
            }
        }
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(programGeomPass, framebuffer)) {
            programGeomPass.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            programGeomPass.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            draw.invoke()
        }
        glBind(listOf(programLightPass, quadMesh, positionStorage, normalStorage, diffuseStorage, depthBuffer)) {
            programLightPass.setUniform(GlUniform.UNIFORM_EYE, camera.position)
            quadMesh.draw()
        }
    }

    fun instance(model: Model) {
        instance(model.mesh, model.diffuse, model.calculateModelM())
    }

    fun instance(mesh: GlMesh, diffuse: GlTexture, modelM: Matrix4f) {
        glBind(listOf(mesh, diffuse)) {
            programGeomPass.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            programGeomPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            mesh.draw()
        }
    }
}