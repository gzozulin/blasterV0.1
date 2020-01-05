package com.blaster.renderers

import com.blaster.assets.AssetStream
import com.blaster.assets.PixelDecoder
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Node
import org.joml.Vector3f

private val backend = GlLocator.instance()

class SimpleRenderer(customPixelDecoder: PixelDecoder? = null) : Renderer  {
    private val assetStream = AssetStream()
    private val pixelDecoder = customPixelDecoder ?: PixelDecoder()

    private val shadersLib = ShadersLib(assetStream)
    private val texturesLib = TexturesLib(assetStream, pixelDecoder)

    private lateinit var program: GlProgram

    private lateinit var mesh: GlMesh

    private lateinit var camera: Camera

    private val node1 = Node()
    private val node2 = Node()

    private val triangleAttributes = listOf(GlAttribute.ATTRIBUTE_POSITION, GlAttribute.ATTRIBUTE_TEXCOORD)
    private val triangleVertices = floatArrayOf(
             0f,  1f, 0f,     0.5f, 0f,
            -1f, -1f, 0f,     0f,   1f,
             1f, -1f, 0f,     1f,   1f
    )
    private val triangleIndices = intArrayOf(0, 1, 2)

    private lateinit var texture: GlTexture

    override fun onCreate() {
        glCheck { backend.glEnable(backend.GL_DEPTH_TEST) }
        glCheck { backend.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) }
        program = shadersLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
        mesh = GlMesh(triangleVertices, triangleIndices, triangleAttributes)
        //texture = texturesLib.loadTexture("models/house/house_diffuse.png")
        texture = texturesLib.loadTexture("textures/winner.png")
        texture = texturesLib.loadTexture("textures/utah.jpeg")
        glBind(program) {
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, texture)
        }
        node1.attach(node2)
    }

    override fun onChange(width: Int, height: Int) {
        glCheck { backend.glViewport(0, 0, width, height) }
        camera = Camera(width.toFloat() / height.toFloat())
        camera.lookAt(Vector3f(0f, 0f, 2.5f), Vector3f())
    }

    override fun onDraw() {
        node1.tick()
        node2.tick()
        glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
        glBind(listOf(program, mesh, texture)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_MODEL_M, node1.calculateModelM())
            mesh.draw()
            program.setUniform(GlUniform.UNIFORM_MODEL_M, node2.calculateModelM())
            mesh.draw()
        }
    }
}