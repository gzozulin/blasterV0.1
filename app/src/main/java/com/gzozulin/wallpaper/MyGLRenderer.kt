package com.gzozulin.wallpaper

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.gzozulin.wallpaper.gl.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {
    private val vertexShaderCode = """
            uniform mat4 uMvp;
            attribute vec4 vPosition;
            void main() {
              gl_Position = uMvp * vPosition;
            }
            """

    private val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 uColor;
            void main() {
              gl_FragColor = uColor;
            }
            """

    private lateinit var program: GLProgram
    private lateinit var triangles: Triangle

    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()
    private lateinit var mvpMatrix: Matrix4f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        triangles = Triangle()
        program = GLProgram(GLShader(ShaderType.VERTEX_SHADER, vertexShaderCode), GLShader(ShaderType.FRAGMENT_SHADER, fragmentShaderCode))
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        program.bind()
        program.sendUniform(ShaderUniform.UNIFORM_COLOR, Vector4f(1f, 0f, 0f, 1f))
        program.sendUniform(ShaderUniform.UNIFORM_MVP, mvpMatrix)
        triangles.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        projectionMatrix.makeFrustum(-ratio, ratio, -1f, 1f, 3f, 7f)
        viewMatrix.makeLookAt(Vector4f(0f, 0f, -3f), Vector4f(), Vector4f(0f, 1f, 0f))
        mvpMatrix = projectionMatrix * viewMatrix
    }
}