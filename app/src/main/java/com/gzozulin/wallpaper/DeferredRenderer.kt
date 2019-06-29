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

    private lateinit var triVerticesBuffer: GLBuffer
    private lateinit var triIndicesBuffer: GLBuffer

    private val quadAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORDS)
    private val quadVertices = floatArrayOf(
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
             1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
             1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    )

    private val quadIndices = intArrayOf(1, 3, 2, 3, 4, 2)

    private lateinit var quadVerticesBuffer: GLBuffer
    private lateinit var quadIndicesBuffer: GLBuffer

    private lateinit var programGeomPass: GLProgram
    private lateinit var programLightPass: GLProgram

    private lateinit var framebuffer: GLFrameBuffer
    private lateinit var texPosition: GLTexture
    private lateinit var texNormal: GLTexture
    private lateinit var texAlbedoSpec: GLTexture

    private lateinit var depthBuffer: GLRenderBuffer

    private lateinit var viewMatrix: Matrix4f
    private lateinit var projectionMatrix: Matrix4f
    private val modelMatrix = Matrix4f()

    private val lightConstant = 1.0f
    private val lightLinear = 1.8f
    private val lightQuadratic = 1.8f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glEnable(GLES30.GL_DEPTH_TEST) }
        triVerticesBuffer = GLBuffer(GLES30.GL_ARRAY_BUFFER, triangleVertices)
        triIndicesBuffer = GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, triangleIndices)
        quadVerticesBuffer = GLBuffer(GLES30.GL_ARRAY_BUFFER, quadVertices)
        quadIndicesBuffer = GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, quadIndices)
        programGeomPass = shaderLib.loadProgram("shaders/deferred/geom_pass.vert", "shaders/deferred/geom_pass.frag")
        programLightPass = shaderLib.loadProgram("shaders/deferred/light_pass.vert", "shaders/deferred/light_pass.frag")
        framebuffer = GLFrameBuffer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val ratio = width.toFloat() / height.toFloat()
        projectionMatrix.makeFrustum(-ratio, ratio, -1f, 1f, 1f, 5f)
        viewMatrix.makeLookAt(Vector3f(0f, 0f, 2.5f), Vector3f(), Vector3f(0f, 1f, 0f))
        glCheck { GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) }
        texPosition = GLTexture(
                active = GLES30.GL_TEXTURE0,
                internalFormat = GLES30.GL_RGB16F, width = width, height = height,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        texNormal = GLTexture(
                active = GLES30.GL_TEXTURE1,
                internalFormat = GLES30.GL_RGB16F, width = width, height = height,
                pixelFormat = GLES30.GL_RGB, pixelType = GLES30.GL_FLOAT)
        texAlbedoSpec = GLTexture(
                active = GLES30.GL_TEXTURE2,
                internalFormat = GLES30.GL_RGBA, width = width, height = height,
                pixelFormat = GLES30.GL_RGBA, pixelType = GLES30.GL_UNSIGNED_BYTE)
        depthBuffer = GLRenderBuffer(width = width, height = height)
        glBind(framebuffer) {
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT0, texPosition)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT1, texNormal)
            framebuffer.setTexture(GLES30.GL_COLOR_ATTACHMENT2, texAlbedoSpec)
            glCheck {
                val attachments = intArrayOf(GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_COLOR_ATTACHMENT2)
                GLES30.glDrawBuffers(attachments.size, attachments, 0)
            }
            framebuffer.setRenderBuffer(GLES30.GL_DEPTH_ATTACHMENT, depthBuffer)
            framebuffer.checkIsComplete()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
        // 1 - geometry pass
        glBind(framebuffer) {
            glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
            glBind(programGeomPass) {
                programGeomPass.setUniform(GLUniform.UNIFORM_PROJECTION, projectionMatrix)
                programGeomPass.setUniform(GLUniform.UNIFORM_VIEW, viewMatrix)
                programGeomPass.setUniform(GLUniform.UNIFORM_MODEL, modelMatrix)
                glBind(listOf(triVerticesBuffer, triIndicesBuffer)) {
                    programGeomPass.setAttributes(triangleAttributes)
                    glCheck { GLES30.glDrawElements(GLES30.GL_TRIANGLES, triangleIndices.size, GLES30.GL_UNSIGNED_INT, 0) }
                }
            }
        }
        // 2 - lighting pass
        glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
        glBind(listOf(programLightPass, texPosition, texNormal, texAlbedoSpec, quadVerticesBuffer, quadIndicesBuffer)) {
            glCheck {
                programLightPass.setAttributes(quadAttributes)
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, quadIndices.size, GLES30.GL_UNSIGNED_INT, 0)
            }
        }
    }
}
