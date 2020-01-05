package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.common.randomVector3f
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Node
import org.joml.Vector3f

private val backend = GlLocator.locate()

class DeferredTechnique {
    private lateinit var programGeomPass: GlProgram
    private lateinit var programLightPass: GlProgram

    // todo: upside down, normalized device space?
    private val quadAttributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD)
    private val quadVertices = floatArrayOf(
            -1f,  1f, 0f,     0f, 1f,
            -1f, -1f, 0f,     0f, 0f,
            1f,  1f, 0f,     1f, 1f,
            1f, -1f, 0f,     1f, 0f
    )
    private val quadIndices = intArrayOf(0, 1, 2, 1, 3, 2)

    private lateinit var quadMesh: GlMesh

    private lateinit var framebuffer: GlFrameBuffer
    private lateinit var positionStorage: GlTexture
    private lateinit var normalStorage: GlTexture
    private lateinit var diffuseStorage: GlTexture

    private lateinit var depthBuffer: GlRenderBuffer

    fun prepare(shadersLib: ShadersLib, width: Int, height: Int) {
        programGeomPass = shadersLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shadersLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        quadMesh = GlMesh(quadVertices, quadIndices, quadAttributes)
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

    fun draw(camera: Camera, renderlist: List<Node>) {
        geometryPass(camera, renderlist)
        lightingPass(camera)
    }

    private fun geometryPass(camera: Camera, renderlist: List<Node>) {
        glBind(listOf(programGeomPass, framebuffer)) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            for (node in renderlist) {
                when (node) {
                    is GlModel -> {
                        glBind(listOf(node.mesh, node.diffuse)) {
                            programGeomPass.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
                            programGeomPass.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
                            programGeomPass.setUniform(GlUniform.UNIFORM_MODEL_M, node.calculateModelM())
                            programGeomPass.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, node.diffuse)
                            node.mesh.draw()
                        }
                    }
                }
            }
        }
    }

    private fun lightingPass(camera: Camera) {
        glBind(listOf(programLightPass, quadMesh, positionStorage, normalStorage, diffuseStorage, depthBuffer)) {
            programLightPass.setUniform(GlUniform.UNIFORM_VIEW_POS, camera.position)
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            quadMesh.draw()
        }
    }
}