package com.blaster.hitables

import com.blaster.bvh.BVHNode
import com.blaster.material.Material
import com.blaster.math.AABB
import com.blaster.math.Ray
import com.blaster.math.Vec3
import com.blaster.tracing.HitRecord

data class Box(
    val min: Vec3, val max: Vec3,
    val leftMaterial: Material, val rightMaterial: Material,
    val topMaterial: Material, val bottomMaterial: Material,
    val frontMaterial: Material, val backMaterial: Material
) : Hitable {

    constructor(min: Vec3, max: Vec3, material: Material) : this(
        min, max, material, material, material, material, material, material)

    private val aabb = AABB(min, max)

    private val node: BVHNode

    init {
        val hitables = ArrayList<Hitable>()
        hitables.add(RectXY(min.x, max.x, min.y, max.y, max.z, backMaterial))
        hitables.add(FlipNormals(RectXY(min.x, max.x, min.y, max.y, min.z, frontMaterial)))
        hitables.add(RectXZ(min.x, max.x, min.z, max.z, max.y, topMaterial))
        hitables.add(FlipNormals(RectXZ(min.x, max.x, min.z, max.z, min.y, bottomMaterial)))
        hitables.add(RectYZ(min.y, max.y, min.z, max.z, max.x, leftMaterial))
        hitables.add(FlipNormals(RectYZ(min.y, max.y, min.z, max.z, min.x, rightMaterial)))
        node = BVHNode(hitables)
    }

    override fun aabb() = aabb

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        return node.hit(ray, tMin, tMax)
    }
}