package com.blaster.techniques

import com.blaster.assets.ShadersLib
import com.blaster.auxiliary.vec3
import com.blaster.entity.Camera
import com.blaster.entity.Light
import com.blaster.entity.PbrMaterial
import com.blaster.gl.GlMesh
import com.blaster.gl.GlProgram
import com.blaster.gl.GlUniform
import com.blaster.gl.glBind
import org.joml.Matrix4f

class PbrTechnique {
    private lateinit var program: GlProgram

    fun create(shadersLib: ShadersLib) {
        program = shadersLib.loadProgram("shaders/pbr/pbr.vert", "shaders/pbr/pbr.frag")
    }

    private var pointLightCnt = 0
    private var dirLightCnt = 0
    fun draw(camera: Camera, lights: () -> Unit, meshes: () -> Unit) {
        glBind(program) {
            program.setUniform(GlUniform.UNIFORM_VIEW_M,    camera.calculateViewM())
            program.setUniform(GlUniform.UNIFORM_PROJ_M,    camera.projectionM)
            program.setUniform(GlUniform.UNIFORM_EYE,       camera.position)
            lights.invoke()
            program.setUniform(GlUniform.UNIFORM_LIGHTS_POINT_CNT, pointLightCnt)
            //program.setUniform(GlUniform.UNIFORM_LIGHTS_DIR_CNT, dirLightCnt)
            meshes.invoke()
        }
        pointLightCnt = 0
        dirLightCnt = 0
    }

    private val lightVectorBuf = vec3()
    fun light(light: Light, modelM: Matrix4f) {
        if (light.point) {
            modelM.getColumn(3, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_VECTOR, pointLightCnt, lightVectorBuf)
            program.setArrayUniform(GlUniform.UNIFORM_LIGHT_INTENSITY, pointLightCnt, light.intensity)
            pointLightCnt++
        } else {
            TODO()
        }
        check(pointLightCnt + dirLightCnt < MAX_LIGHTS) { "More lights than defined in shader!" }
    }

    fun instance(mesh: GlMesh, modelM: Matrix4f, material: PbrMaterial) {
        glBind(listOf(mesh, material.albedo, material.normal, material.metallic, material.roughness, material.ao)) {
            program.setUniform(GlUniform.UNIFORM_MODEL_M,           modelM)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ALBEDO,    material.albedo)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_NORMAL,    material.normal)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_METALLIC,  material.metallic)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_ROUGHNESS, material.roughness)
            program.setTexture(GlUniform.UNIFORM_TEXTURE_AO,        material.ao)
            mesh.draw()
        }
    }
}