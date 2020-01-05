package com.blaster.ofc

import com.blaster.platform.LwjglWindow
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

// todo templates -> setup on the scene

// template model
// template light
// template billboard
// template ps
// etc

// general placeholders (aabb) placement

/*private val renderer = DeferredRenderer()

private val window = object : LwjglWindow(WIDTH, HEIGHT) {
    override fun onCreate(width: Int, height: Int) {
        renderer.onCreate()
        renderer.onChange(width, height)
    }

    override fun onDraw() {
        renderer.onDraw()
    }
}*/

data class MaterialTemplate(
        val ambientTerm: Vector3f, val specularPower: Float, val diffuseFilename: String)

data class ModelTemplate(
        val meshFilename: String, val material: MaterialTemplate,
        val position: Vector3f, val rotation: Quaternionf, val scale: Vector3f)

data class LightTemplate(val vector: Vector3f, val color: Vector3f,
                         val constantAttenuation: Float, val linearAttenuation: Float, val quadraticAttenuation: Float)

class SceneTemplate {

}

fun main() {
    //window.show()
}
