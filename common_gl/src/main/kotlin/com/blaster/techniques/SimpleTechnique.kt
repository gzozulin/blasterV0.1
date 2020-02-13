package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.gl.*
import com.blaster.entity.Camera
import com.blaster.gl.GlMesh
import com.blaster.entity.Model
import org.joml.Matrix4f

// A most straightforward technique: no lighting, just diffuse from textures
class SimpleTechnique {
    private lateinit var program: GlProgram

    // We want to create the shader program first
    fun create(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
    }

    // While drawing, we pass the uniforms for the whole pass
    fun draw(camera: Camera, draw: () -> Unit) {
        glBind(listOf(program)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            draw.invoke()
        }
    }

    fun instance(model: Model, modelM: Matrix4f) {
        instance(model.mesh, model.diffuse, modelM)
    }

    // For each instance we pass unique uniforms: model matrix and diffuse texture handle
    fun instance(mesh: GlMesh, diffuse: GlTexture, modelM: Matrix4f) {
        glBind(listOf(mesh, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            mesh.draw()
        }
    }
}