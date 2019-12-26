package com.blaster.hitables

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.Material
import com.blaster.scene.Aabb
import com.blaster.scene.Ray
import com.blaster.scene.Vec3
import com.gzozulin.bvh.BvhNode

data class BoxHitable(
        val min: Vec3, val max: Vec3,
        val leftMaterial: Material, val rightMaterial: Material,
        val topMaterial: Material, val bottomMaterial: Material,
        val frontMaterial: Material, val backMaterial: Material
) : Hitable {

    constructor(min: Vec3, max: Vec3, material: Material)
            : this(min, max, material, material, material, material, material, material)

    private val aabb = Aabb(min, max)

    private val node: BvhNode

    init {
        val hitables = mutableListOf<Hitable>()
        hitables.add(RectXYHitable(min.x, max.x, min.y, max.y, max.z, backMaterial))
        hitables.add(FlipNormals(RectXYHitable(min.x, max.x, min.y, max.y, min.z, frontMaterial)))
        hitables.add(RectXZHitable(min.x, max.x, min.z, max.z, max.y, topMaterial))
        hitables.add(FlipNormals(RectXZHitable(min.x, max.x, min.z, max.z, min.y, bottomMaterial)))
        hitables.add(RectYZHitable(min.y, max.y, min.z, max.z, max.x, leftMaterial))
        hitables.add(FlipNormals(RectYZHitable(min.y, max.y, min.z, max.z, min.x, rightMaterial)))
        node = BvhNode(hitables)
    }

    override fun aabb() = aabb

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        return node.hit(ray, tMin, tMax)
    }
}