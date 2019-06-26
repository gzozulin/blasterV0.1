package com.gzozulin.wallpaper

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.gzozulin.wallpaper.gl.*
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(ctx: Context) : GLSurfaceView.Renderer  {
    private var shaderLib = ShaderLib(ctx)

    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     1f, 0f, 0f,
            -1f, -1f, 0f,     0f, 1f, 0f,
             1f, -1f, 0f,     0f, 0f, 1f
    )

    private val triangleIndices = intArrayOf(0, 1, 2)

    private lateinit var program: GLProgram

    private val modelMatrix = Matrix4f()
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()

    private lateinit var geometry: GLGeometry

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glCullFace(GLES20.GL_FRONT_AND_BACK)
        program = shaderLib.loadShader("shaders/simple.vert", "shaders/simple.frag")
        geometry = GLGeometry(triangleVertices, triangleIndices, GLES20.GL_TRIANGLES)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        projectionMatrix.makeFrustum(-ratio, ratio, -1f, 1f, 1f, 5f)
        viewMatrix.makeLookAt(Vector3f(0f, 0f, 2.5f), Vector3f(), Vector3f(0f, 1f, 0f))
    }

    var last = System.currentTimeMillis()

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val current = System.currentTimeMillis()
        val elapsed = current - last
        last = current
        modelMatrix.rotateInplace(0.1f * elapsed.toFloat(), Vector3f(0f, 1f, 0f))
        val mvpMatrix = projectionMatrix * viewMatrix * modelMatrix
        geometry.bind()
        program.bind()
        program.sendAttributes(listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_COLOR))
        program.sendUniform(GLUniform.UNIFORM_MVP, mvpMatrix)
        geometry.draw()
    }
}