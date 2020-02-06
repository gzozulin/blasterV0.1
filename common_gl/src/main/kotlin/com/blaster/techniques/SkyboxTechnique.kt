package com.blaster.techniques

import com.blaster.assets.MeshLib
import com.blaster.assets.ShadersLib
import com.blaster.assets.TexturesLib
import com.blaster.common.mat3
import com.blaster.common.mat4
import com.blaster.gl.GlProgram
import com.blaster.gl.GlTexture
import com.blaster.gl.GlUniform
import com.blaster.gl.glBind
import com.blaster.entity.Camera
import com.blaster.tools.Mesh

class SkyboxTechnique {
    private lateinit var program: GlProgram
    private lateinit var diffuse: GlTexture
    private lateinit var cube: Mesh

    fun create(shadersLib: ShadersLib, textureLib: TexturesLib, meshLib: MeshLib, skybox: String) {
        program = shadersLib.loadProgram("shaders/skybox/skybox.vert", "shaders/skybox/skybox.frag")
        diffuse = textureLib.loadSkybox(skybox)
        val (mesh, _) = meshLib.loadMesh("models/cube/cube.obj")
        cube = mesh
    }

    private val onlyRotationM = mat3()
    private val noTranslationM = mat4()

    fun skybox(camera: Camera) {
        onlyRotationM.set(camera.calculateViewM())
        noTranslationM.set(onlyRotationM)
        glBind(listOf(program, cube, diffuse)) {
            program.setUniform(GlUniform.UNIFORM_PROJ_M, camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_VIEW_M, noTranslationM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_DIFFUSE, diffuse)
            cube.draw()
        }
    }
}