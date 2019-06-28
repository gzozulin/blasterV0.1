package com.gzozulin.wallpaper

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.gzozulin.wallpaper.gl.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRenderer(ctx: Context) : GLSurfaceView.Renderer  {
    private var shaderLib = ShaderLib(ctx)

    private lateinit var program: GLProgram

    private val triangleAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_COLOR)
    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     1f, 0f, 0f,
            -1f, -1f, 0f,     0f, 1f, 0f,
             1f, -1f, 0f,     0f, 0f, 1f
    )
    private val triangleIndices = intArrayOf(0, 1, 2)

    private lateinit var verticesBuffer: GLBuffer
    private lateinit var indicesBuffer: GLBuffer

    private val modelMatrix = Matrix4f()
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glClearColor(1.0f, 1.0f, 0.0f, 1.0f) }
        program = shaderLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
        verticesBuffer = GLBuffer(GLES30.GL_ARRAY_BUFFER, triangleVertices)
        indicesBuffer = GLBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, triangleIndices)
        glBind(verticesBuffer) {
            program.setAttributes(triangleAttributes)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glCheck { GLES30.glViewport(0, 0, width, height) }
        val ratio = width.toFloat() / height.toFloat()
        projectionMatrix.makeFrustum(-ratio, ratio, -1f, 1f, 1f, 5f)
        viewMatrix.makeLookAt(Vector3f(0f, 0f, 2.5f), Vector3f(), Vector3f(0f, 1f, 0f))
    }

    private var last = System.currentTimeMillis()
    private fun calculateMvp(): Matrix4f {
        val current = System.currentTimeMillis()
        val elapsed = current - last
        last = current
        modelMatrix.rotateInplace(0.1f * elapsed.toFloat(), Vector3f(0f, 1f, 0f))
        return projectionMatrix * viewMatrix * modelMatrix
    }

    override fun onDrawFrame(gl: GL10?) {
        glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT) }
        glBind(listOf(verticesBuffer, indicesBuffer, program)) {
            program.setUniform(GLUniform.UNIFORM_MVP, calculateMvp())
            glCheck { GLES30.glDrawElements(GLES30.GL_TRIANGLES, triangleIndices.size, GLES30.GL_UNSIGNED_INT, 0) }
        }
    }
}