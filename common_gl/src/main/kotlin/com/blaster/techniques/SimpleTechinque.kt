package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.gl.*
import com.blaster.scene.Camera
import com.blaster.scene.Model
import org.joml.Matrix4f

class SimpleTechnique {
    private lateinit var program: GlProgram

    fun prepare(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(program)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            draw.invoke()
        }
    }

    fun instance(model: Model) {
        instance(model.mesh, model.diffuse, model.calculateModelM())
    }

    fun instance(mesh: GlMesh, diffuse: GlTexture, modelM: Matrix4f) {
        glBind(listOf(mesh, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            mesh.draw()
        }
    }
}