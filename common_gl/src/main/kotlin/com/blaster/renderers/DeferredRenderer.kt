package com.blaster.renderers

import com.blaster.assets.*
import com.blaster.gl.*
import com.blaster.math.randomVector3f
import com.blaster.scene.Camera
import com.blaster.scene.Node
import org.joml.Vector3f

private val backend = GLLocator.instance()

class DeferredRenderer(assetStream: AssetStream = AssetStream()) : Renderer {
    private val shadersLib = ShadersLib(assetStream)

    // todo: upside down, normalized device space?
    private val quadAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD)
    private val quadVertices = floatArrayOf(
            -1f,  1f, 0f,     0f, 1f,
            -1f, -1f, 0f,     0f, 0f,
             1f,  1f, 0f,     1f, 1f,
             1f, -1f, 0f,     1f, 0f
    )
    private val quadIndices = intArrayOf(0, 1, 2, 1, 3, 2)

    private lateinit var quadMesh: GLMesh

    private lateinit var programGeomPass: GLProgram
    private lateinit var programLightPass: GLProgram

    private lateinit var framebuffer: GLFrameBuffer
    private lateinit var positionStorage: GLTexture
    private lateinit var normalStorage: GLTexture
    private lateinit var diffuseStorage: GLTexture

    private lateinit var depthBuffer: GLRenderBuffer

    val root = Node()
    lateinit var camera: Camera

    private val renderList = mutableListOf<Node>()
    private var renderListVersion = Int.MAX_VALUE

    override fun onCreate() {
        glCheck { backend.glClearColor(0.9f, 0.9f, 1f, 0f) }
        glCheck { backend.glEnable(backend.GL_DEPTH_TEST) }
        glCheck { backend.glFrontFace(backend.GL_CCW) }
        glCheck { backend.glEnable(backend.GL_CULL_FACE) }
        quadMesh = GLMesh(quadVertices, quadIndices, quadAttributes)
        programGeomPass = shadersLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shadersLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
    }

    override fun onChange(width: Int, height: Int) {
        glCheck { backend.glViewport(0, 0, width, height) }
        camera = Camera(width.toFloat() / height.toFloat())
        positionStorage = GLTexture(
                unit = 0,
                width = width, height = height, internalFormat = backend.GL_RGBA16F,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_FLOAT)
        normalStorage = GLTexture(
                unit = 1,
                width = width, height = height, internalFormat = backend.GL_RGB16F,
                pixelFormat = backend.GL_RGB, pixelType = backend.GL_FLOAT)
        diffuseStorage = GLTexture(
                unit = 2,
                width = width, height = height, internalFormat = backend.GL_RGBA,
                pixelFormat = backend.GL_RGBA, pixelType = backend.GL_UNSIGNED_BYTE)
        depthBuffer = GLRenderBuffer(width = width, height = height)
        framebuffer = GLFrameBuffer()
        glBind(listOf(framebuffer)) {
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(backend.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setOutputs(intArrayOf(backend.GL_COLOR_ATTACHMENT0, backend.GL_COLOR_ATTACHMENT1, backend.GL_COLOR_ATTACHMENT2))
            framebuffer.setRenderBuffer(backend.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glBind(programLightPass) {
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_POSITION, positionStorage)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_NORMAL, normalStorage)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, diffuseStorage)
            programLightPass.setUniform(GLUniform.UNIFORM_VIEW_POS, camera.position)
            for (i in 0..15) {
                val randomPos = randomVector3f(Vector3f(-5f), Vector3f(5f))
                val randomColor = randomVector3f(Vector3f(), Vector3f(1f))
                programLightPass.setUniform(GLUniform.uniformLightPosition(i), randomPos)
                programLightPass.setUniform(GLUniform.uniformLightColor(i), randomColor)
            }
        }
    }

    override fun onDraw() {
        updateRenderList()
        geometryPass()
        lightingPass()
    }

    private fun updateRenderList() {
        if (renderListVersion != root.graphVersion) {
            renderList.clear()
            addChildrenToRenderlist(root)
            renderListVersion = root.graphVersion
        }
    }

    private fun addChildrenToRenderlist(node: Node) {
        renderList.addAll(node.children)
        node.children.forEach { addChildrenToRenderlist(it) }
    }

    private fun geometryPass() {
        glBind(listOf(programGeomPass, framebuffer)) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            for (node in renderList) {
                when (node) {
                    is GLModel -> {
                        glBind(listOf(node.mesh, node.diffuse)) {
                            programGeomPass.setUniform(GLUniform.UNIFORM_VIEW_M, camera.calculateM())
                            programGeomPass.setUniform(GLUniform.UNIFORM_PROJ_M, camera.projectionM)
                            programGeomPass.setUniform(GLUniform.UNIFORM_MODEL_M, node.calculateM())
                            programGeomPass.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, node.diffuse)
                            node.mesh.draw()
                        }
                    }
                }
            }
        }
    }

    private fun lightingPass() {
        glBind(listOf(programLightPass, quadMesh, positionStorage, normalStorage, diffuseStorage, depthBuffer)) {
            glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
            quadMesh.draw()
        }
    }
}
