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
             0.0f,  0.622008459f, 0.0f,
            -0.5f, -0.311004243f, 0.0f,
             0.5f, -0.311004243f, 0.0f
    )

    private val triangleIndices = intArrayOf(
            0, 1, 2
    )

    private lateinit var program: GLProgram

    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()
    private lateinit var mvpMatrix: Matrix4f

    private lateinit var geometry: GLGeometry

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        program = shaderLib.loadShader("shaders/simple.vert", "shaders/simple.frag")
        geometry = GLGeometry(triangleVertices, triangleIndices, Collections.singletonList(GLAttribute.ATTRIBUTE_POSITION), GLES20.GL_TRIANGLES)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        projectionMatrix.makeFrustum(-ratio, ratio, -1f, 1f, 3f, 7f)
        viewMatrix.makeLookAt(Vector4f(0f, 0f, -3f), Vector4f(), Vector4f(0f, 1f, 0f))
        mvpMatrix = projectionMatrix * viewMatrix
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        program.bind()
        program.sendUniform(GLUniform.UNIFORM_COLOR, Vector4f(1f, 0f, 0f, 1f))
        program.sendUniform(GLUniform.UNIFORM_MVP, mvpMatrix)
        geometry.bind()
        geometry.draw()
    }
}