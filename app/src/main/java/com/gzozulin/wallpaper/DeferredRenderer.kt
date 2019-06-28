package com.gzozulin.wallpaper

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.gzozulin.wallpaper.gl.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DeferredRenderer(context: Context) : GLSurfaceView.Renderer {
    private val shaderLib = ShaderLib(context)

    private val triangleAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_COLOR)
    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     1f, 0f, 0f,
            -1f, -1f, 0f,     0f, 1f, 0f,
             1f, -1f, 0f,     0f, 0f, 1f
    )
    private val triangleIndices = intArrayOf(0, 1, 2)

    private lateinit var verticesBuffer: GLBuffer
    private lateinit var indicesBuffer: GLBuffer

    private lateinit var programGeomPass: GLProgram
    private lateinit var programLightPass: GLProgram

    private lateinit var framebuffer: GLFrameBuffer
    private lateinit var texPosition: GLTexture
    private lateinit var texNormal: GLTexture
    private lateinit var texAlbedoSpec: GLTexture

    private lateinit var depthBuffer: GLRenderBuffer

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glEnable(GLES30.GL_DEPTH_TEST) }
        verticesBuffer = GLBuffer(GLES30.GL_ARRAY_BUFFER, triangleVertices)
        indicesBuffer = GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, triangleIndices)
        programGeomPass = shaderLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shaderLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        framebuffer = GLFrameBuffer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        texPosition = GLTexture(
                internalFormat = GLES30.GL_RGB16F, width = width, height = height,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        texNormal = GLTexture(
                internalFormat = GLES30.GL_RGB16F, width = width, height = height,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        texAlbedoSpec = GLTexture(
                internalFormat = GLES30.GL_RGBA, width = width, height = height,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_UNSIGNED_BYTE)
        depthBuffer = GLRenderBuffer(width = width, height = height)
        glBind(framebuffer) {
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT0, texPosition)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT1, texNormal)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT2, texAlbedoSpec)
            framebuffer.setRenderBuffer(GLES30.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
        glCheck {
            val attachments = intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2)
            GLES30.glDrawBuffers(attachments.size, attachments, 0)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        // implement me!
    }
}
