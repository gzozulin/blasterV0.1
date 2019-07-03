package com.gzozulin.wallpaper.renderers

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.gzozulin.wallpaper.assets.ShaderLib
import com.gzozulin.wallpaper.assets.TextureLib
import com.gzozulin.wallpaper.gl.*
import com.gzozulin.wallpaper.math.SceneCamera
import com.gzozulin.wallpaper.math.SceneNode
import com.gzozulin.wallpaper.math.Vector3f
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DeferredRenderer(context: Context) : GLSurfaceView.Renderer {
    private val shaderLib = ShaderLib(context)
    private val textureLib = TextureLib(context)

    private val triangleAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD, GLAttribute.ATTRIBUTE_NORMAL)
    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     0.5f, 0f,      0f, 1f, 0f,
            -1f, -1f, 0f,     0f,   1f,      0f, 1f, 0f,
             1f, -1f, 0f,     1f,   1f,      0f, 1f, 0f
    )
    private val triangleIndices = intArrayOf(0, 2, 1)

    // todo: upside down
    private val quadAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD)
    private val quadVertices = floatArrayOf(
            -1f,  1f, 0f,     0f, 1f,
            -1f, -1f, 0f,     0f, 0f,
             1f,  1f, 0f,     1f, 1f,
             1f, -1f, 0f,     1f, 0f
    )
    private val quadIndices = intArrayOf(0, 2, 1, 2, 3, 1)

    private lateinit var triangleMesh :GLMesh
    private lateinit var quadMesh: GLMesh

    private lateinit var textureDiffuse: GLTexture
    private lateinit var textureSpecular: GLTexture

    private lateinit var programGeomPass: GLProgram
    private lateinit var programLightPass: GLProgram

    private lateinit var framebuffer: GLFrameBuffer
    private lateinit var positionTexture: GLTexture
    private lateinit var normalTexture: GLTexture
    private lateinit var albedoSpecTexture: GLTexture

    private lateinit var depthBuffer: GLRenderBuffer

    private lateinit var camera: SceneCamera
    private val eye = Vector3f(z = 3f)
    private val node = SceneNode()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glEnable(GLES30.GL_DEPTH_TEST) }
        glCheck { GLES30.glClearColor(1f, 1f, 1f, 1f) }
        triangleMesh = GLMesh(triangleVertices, triangleIndices, triangleAttributes)
        quadMesh = GLMesh(quadVertices, quadIndices, quadAttributes)
        textureDiffuse = textureLib.loadTexture("textures/winner.png")
        textureSpecular = textureLib.loadTexture("textures/winner.png")
        programGeomPass = shaderLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shaderLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glCheck { GLES30.glViewport(0, 0, width, height) }
        camera = SceneCamera(width.toFloat() / height.toFloat())
        camera.lookAt(eye, Vector3f())
        positionTexture = GLTexture(
                unit = 0,
                width = width, height = height, internalFormat = GLES30.GL_RGB16F,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        normalTexture = GLTexture(
                unit = 1,
                width = width, height = height, internalFormat = GLES30.GL_RGB16F,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        albedoSpecTexture = GLTexture(
                unit = 2,
                width = width, height = height, internalFormat = GLES30.GL_RGBA,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_UNSIGNED_BYTE)
        depthBuffer = GLRenderBuffer(width = width, height = height)
        framebuffer = GLFrameBuffer()
        glBind(listOf(framebuffer)) {
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT0, positionTexture)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT1, normalTexture)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT2, albedoSpecTexture)
            framebuffer.setOutputs(intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2))
            framebuffer.setRenderBuffer(GLES30.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glBind(programGeomPass) {
            programGeomPass.setUniform(GLUniform.UNIFORM_MODEL_M, node.calculateViewM())
            programGeomPass.setUniform(GLUniform.UNIFORM_VIEW_M, camera.viewM)
            programGeomPass.setUniform(GLUniform.UNIFORM_PROJ_M, camera.projectionM)
            programGeomPass.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, textureDiffuse)
            programGeomPass.setTexture(GLUniform.UNIFORM_TEXTURE_SPECULAR, textureSpecular)
        }
        glBind(programLightPass) {
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_POSITION, positionTexture)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_NORMAL, normalTexture)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_ALBEDO_SPEC, albedoSpecTexture)
            programLightPass.setUniform(GLUniform.UNIFORM_VIEW_POS, eye)
            setupLights()
        }
    }

    private fun setupLights() {
        for (i in 0..15) {
            programLightPass.setUniform(uniformLightPosition(i), Vector3f().randomize(5f))
            programLightPass.setUniform(uniformLightColor(i), Vector3f().randomize(1f))
        }
    }

    private fun geometryPass() {
        glBind(listOf(programGeomPass, triangleMesh, framebuffer, textureDiffuse, textureSpecular)) {
            glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
            programGeomPass.setUniform(GLUniform.UNIFORM_MODEL_M, node.calculateViewM())
            triangleMesh.draw()
        }
    }

    private fun lightingPass() {
        glBind(listOf(
                programLightPass, quadMesh,
                positionTexture, normalTexture, albedoSpecTexture, depthBuffer
        )) {
            glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
            quadMesh.draw()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        node.tick()
        geometryPass()
        lightingPass()
    }
}
