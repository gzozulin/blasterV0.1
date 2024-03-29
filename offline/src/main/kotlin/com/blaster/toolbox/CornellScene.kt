package com.blaster.toolbox

import com.blaster.hitables.*
import com.blaster.material.DiffuseMaterial
import com.blaster.material.LambertianMaterial
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.texture.ConstantTexture
import com.blaster.texture.ImageTexture
import com.blaster.bvh.BvhNode

class CornellScene : Hitable {
    private val scene: Hitable = prepare()

    private fun prepare(): Hitable {
        val hitables = ArrayList<Hitable>()
        val red = LambertianMaterial(ConstantTexture(Vec3(0.65f, 0.05f, 0.05f)))
        val white = LambertianMaterial(ConstantTexture(Vec3(0.73f)))
        val green = LambertianMaterial(ConstantTexture(Vec3(0.12f, 0.45f, 0.15f)))
        val light = DiffuseMaterial(ConstantTexture(Vec3(15f)))
        val minorLight = DiffuseMaterial(ConstantTexture(Vec3(1f)))
        hitables.add(RectXZHitable(213f, 343f, 227f, 332f, 554f, light))
        hitables.add(FlipNormals(RectYZHitable(0f, 555f, 0f, 555f, 555f, green)))
        hitables.add(RectYZHitable(0f, 555f, 0f, 555f, 0f, red))
        hitables.add(FlipNormals(RectXZHitable(0f, 555f, 0f, 555f, 555f, white)))
        hitables.add(RectXZHitable(0f, 555f, 0f, 555f, 0f, white))
        hitables.add(FlipNormals(RectXYHitable(0f, 555f, 0f, 555f, 555f, white)))
        val leftBox = BoxHitable(Vec3(265f, 0f, 295f), Vec3(430f, 165f, 460f), minorLight)
        hitables.add(leftBox)
        val rightBox = BoxHitable(Vec3(130f, 0f, 65f), Vec3(295f, 165f, 230f), white)
        hitables.add(rightBox)
        val leftBoxCenter = leftBox.aabb().center
        hitables.add(
            SphereHitable(
                    Vec3(leftBoxCenter.x, 300f, leftBoxCenter.z), 90f,
                LambertianMaterial(ImageTexture("offline/earth.png"))
            )
        )
        val fog = BoxHitable(Vec3(), Vec3(555f, 100f, 555f), white)
        hitables.add(MediumHitable(fog, 0.01f, ConstantTexture(Vec3(0.1f, 0.1f, 0.3f))))
        return BvhNode(hitables)
    }

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? = scene.hit(ray, tMin, tMax)

    override fun aabb() = scene.aabb()
}