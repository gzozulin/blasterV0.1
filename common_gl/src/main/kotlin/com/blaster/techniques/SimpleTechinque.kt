package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Node

private val backend = GlLocator.locate()

class SimpleTechnique {
    private lateinit var program: GlProgram

    fun prepare(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")

    }

    fun draw(camera: Camera, renderlist: List<Node>) {
        glCheck { backend.glClear(backend.GL_COLOR_BUFFER_BIT or backend.GL_DEPTH_BUFFER_BIT) }
        glBind(listOf(program)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            for (node in renderlist) {
                when (node) {
                    is GlModel -> {
                        glBind(listOf(node.diffuse, node.mesh)) {
                            program.setUniform(GlUniform.UNIFORM_MODEL_M, node.calculateModelM())
                            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, node.diffuse)
                            node.mesh.draw()
                        }
                    }
                }
            }
        }
    }
}