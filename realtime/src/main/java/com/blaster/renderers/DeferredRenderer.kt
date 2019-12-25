package com.blaster.renderers

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.blaster.assets.ModelsLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Vec3
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.system.measureNanoTime

class DeferredRenderer(context: Context) : GLSurfaceView.Renderer {
    private val shaderLib = ShadersLib(context)
    private val textureLib = TexturesLib(context)
    private val modelsLib = ModelsLib(context, textureLib)

    private lateinit var model: GLModel

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

    private lateinit var camera: Camera

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glClearColor(0.9f, 0.9f, 1f, 0f) }
        glCheck { GLES30.glEnable(GLES30.GL_DEPTH_TEST) }
        glCheck { GLES30.glFrontFace(GLES30.GL_CCW) }
        glCheck { GLES30.glEnable(GLES30.GL_CULL_FACE) }
        quadMesh = GLMesh(quadVertices, quadIndices, quadAttributes)
        programGeomPass = shaderLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shaderLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        val modelNanos = measureNanoTime {
            model = modelsLib.loadModel("models/akai/akai.obj", "models/akai/akai.png")
        }
        Log.i("DeferredRenderer", "Model loaded in ${TimeUnit.NANOSECONDS.toMillis(modelNanos)} millis")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glCheck { GLES30.glViewport(0, 0, width, height) }
        camera = Camera(width.toFloat() / height.toFloat())
        camera.lookAt(model.aabb)
        positionStorage = GLTexture(
                unit = 0,
                width = width, height = height, internalFormat = GLES30.GL_RGBA16F,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_FLOAT)
        normalStorage = GLTexture(
                unit = 1,
                width = width, height = height, internalFormat = GLES30.GL_RGB16F,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        diffuseStorage = GLTexture(
                unit = 2,
                width = width, height = height, internalFormat = GLES30.GL_RGBA,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_UNSIGNED_BYTE)
        depthBuffer = GLRenderBuffer(width = width, height = height)
        framebuffer = GLFrameBuffer()
        glBind(listOf(framebuffer)) {
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT0, positionStorage)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT1, normalStorage)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT2, diffuseStorage)
            framebuffer.setOutputs(intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2))
            framebuffer.setRenderBuffer(GLES30.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glBind(programGeomPass) {
            programGeomPass.setUniform(GLUniform.UNIFORM_MODEL_M, model.node.calculateViewM())
            programGeomPass.setUniform(GLUniform.UNIFORM_VIEW_M, camera.viewM)
            programGeomPass.setUniform(GLUniform.UNIFORM_PROJ_M, camera.projectionM)
            programGeomPass.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, model.diffuse)
        }
        glBind(programLightPass) {
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_POSITION, positionStorage)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_NORMAL, normalStorage)
            programLightPass.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, diffuseStorage)
            programLightPass.setUniform(GLUniform.UNIFORM_VIEW_POS, camera.eye)
            setupLights()
        }
    }

    private fun setupLights() {
        for (i in 0..15) {
            programLightPass.setUniform(uniformLightPosition(i), Vec3.randomVec3(5f))
            programLightPass.setUniform(uniformLightColor(i), Vec3.randomVec3(1f))
        }
    }

    private fun geometryPass() {
        glBind(listOf(programGeomPass, model.mesh, framebuffer, model.diffuse)) {
            glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
            programGeomPass.setUniform(GLUniform.UNIFORM_MODEL_M, model.node.calculateViewM())
            model.mesh.draw()
        }
    }

    private fun lightingPass() {
        glBind(listOf(programLightPass, quadMesh, positionStorage, normalStorage, diffuseStorage, depthBuffer)) {
            glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
            quadMesh.draw()
        }
    }

    private var fps = 0
    private var last = System.currentTimeMillis()

    private fun printFps() {
        fps++
        val current = System.currentTimeMillis()
        if (current - last >= 1000L) {
            Log.i("DeferredRenderer", "Fps: $fps")
            fps = 0
            last = current
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        model.node.tick()
        geometryPass()
        lightingPass()
        printFps()
    }
}
