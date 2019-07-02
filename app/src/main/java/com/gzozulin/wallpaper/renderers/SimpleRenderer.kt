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

class SimpleRenderer(ctx: Context) : GLSurfaceView.Renderer  {
    private val shaderLib = ShaderLib(ctx)
    private val textureLib = TextureLib(ctx)

    private lateinit var program: GLProgram

    private lateinit var mesh: GLMesh

    private lateinit var camera: SceneCamera

    private val node1 = SceneNode()
    private val node2 = SceneNode(node1)

    private val triangleAttributes = listOf(GLAttribute.ATTRIBUTE_POSITION, GLAttribute.ATTRIBUTE_TEXCOORD)
    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     0.5f, 0f,
            -1f, -1f, 0f,     0f,   1f,
             1f, -1f, 0f,     1f,   1f
    )
    private val triangleIndices = intArrayOf(0, 1, 2)

    private lateinit var texture: GLTexture

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glCheck { GLES30.glEnable(GLES30.GL_DEPTH_TEST) }
        glCheck { GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) }
        program = shaderLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
        mesh = GLMesh(triangleVertices, triangleIndices, triangleAttributes)
        texture = textureLib.loadTexture("textures/winner.png")
        glBind(program) {
            program.setTexture(GLUniform.UNIFORM_TEXTURE_DIFFUSE, texture)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glCheck { GLES30.glViewport(0, 0, width, height) }
        camera = SceneCamera(width.toFloat() / height.toFloat())
        camera.lookAt(Vector3f(z = 2.5f), Vector3f())
    }

    override fun onDrawFrame(gl: GL10?) {
        node1.tick()
        node2.tick()
        glCheck { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT) }
        glBind(listOf(program, mesh, texture)) {
            program.setUniform(GLUniform.UNIFORM_VIEW_M, camera.viewM)
            program.setUniform(GLUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GLUniform.UNIFORM_MODEL_M, node1.calculateViewM())
            mesh.draw()
            program.setUniform(GLUniform.UNIFORM_MODEL_M, node2.calculateViewM())
            mesh.draw()
        }
    }
}