package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.auxiliary.mat4
import com.blaster.gl.*
import com.blaster.entity.Camera
import com.blaster.gl.GlMesh
import com.blaster.entity.Model
import org.joml.Matrix4f

// A most straightforward technique: no lighting, just diffuse from textures
class SimpleTechnique {
    private lateinit var program: GlProgram

    // Compiling and storing shader program
    fun create(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/simple/no_lighting.vert", "shaders/simple/no_lighting.frag")
    }

    // While drawing, we pass the uniforms for the whole pass
    fun draw(viewM: mat4, projectionM: mat4, draw: () -> Unit) {
        glBind(listOf(program)) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M, viewM)
            program.setUniform(GlUniform.UNIFORM_PROJ_M, projectionM)
            draw.invoke()
        }
    }

    fun draw(camera: Camera, draw: () -> Unit) {
        draw(camera.calculateViewM(), camera.projectionM, draw)
    }

    fun instance(model: Model, modelM: Matrix4f) {
        instance(model.mesh, model.diffuse, modelM)
    }

    // For each instance we pass unique uniforms: model matrix and diffuse texture handle
    fun instance(mesh: GlMesh, diffuse: GlTexture, modelM: Matrix4f) {
        glBind(listOf(mesh, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M, modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            // After uniforms are ready, we can make a call to render the geometry
            mesh.draw()
        }
    }
}