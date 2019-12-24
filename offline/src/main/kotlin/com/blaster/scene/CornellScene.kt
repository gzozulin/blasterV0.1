package com.blaster.scene

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.hitables.*
import com.blaster.material.Diffuse
import com.blaster.material.Lambertian
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.texture.ConstantTexture
import com.blaster.texture.ImageTexture
import com.gzozulin.bvh.BvhNode

class CornellScene : Hitable {
    private val scene: Hitable = prepare()

    private fun prepare(): Hitable {
        val hitables = ArrayList<Hitable>()
        val red = Lambertian(ConstantTexture(Vec3(0.65f, 0.05f, 0.05f)))
        val white = Lambertian(ConstantTexture(Vec3(0.73f)))
        val green = Lambertian(ConstantTexture(Vec3(0.12f, 0.45f, 0.15f)))
        val light = Diffuse(ConstantTexture(Vec3(15f)))
        val minorLight = Diffuse(ConstantTexture(Vec3(1f)))
        hitables.add(RectXZ(213f, 343f, 227f, 332f, 554f, light))
        hitables.add(FlipNormals(RectYZ(0f, 555f, 0f, 555f, 555f, green)))
        hitables.add(RectYZ(0f, 555f, 0f, 555f, 0f, red))
        hitables.add(FlipNormals(RectXZ(0f, 555f, 0f, 555f, 555f, white)))
        hitables.add(RectXZ(0f, 555f, 0f, 555f, 0f, white))
        hitables.add(FlipNormals(RectXY(0f, 555f, 0f, 555f, 555f, white)))
        val leftBox = Box(Vec3(265f, 0f, 295f), Vec3(430f, 165f, 460f), minorLight)
        hitables.add(leftBox)
        val rightBox = Box(Vec3(130f, 0f, 65f), Vec3(295f, 165f, 230f), white)
        hitables.add(rightBox)
        val leftBoxCenter = leftBox.aabb().center
        hitables.add(
            Sphere(
                Vec3(leftBoxCenter.x, 300f, leftBoxCenter.z), 90f,
                Lambertian(ImageTexture("offline/earth.png"))
            )
        )
        val fog = Box(Vec3(), Vec3(555f, 100f, 555f), white)
        hitables.add(ConstantMedium(fog, 0.01f, ConstantTexture(Vec3(0.1f, 0.1f, 0.3f))))
        return BvhNode(hitables)
    }

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? = scene.hit(ray, tMin, tMax)

    override fun aabb() = scene.aabb()
}