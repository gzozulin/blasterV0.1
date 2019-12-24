package com.gzozulin.bvh

import com.blaster.HitRecord
import com.blaster.Hitable
import com.blaster.scene.Aabb
import com.blaster.scene.Ray

class BvhNode(hitables: List<Hitable>) : Hitable {
    private val aabb: Aabb

    init {
        check(hitables.isNotEmpty())
        var accumulator = hitables.first().aabb()
        for (hitable in hitables) {
            accumulator += hitable.aabb()
        }
        aabb = accumulator
    }

    private val left: BvhNode?
    private val right: BvhNode?

    private val children: Array<Hitable>?

    init {
        if (hitables.size > BVH_THRESHOLD) {
            children = null
            val maxAxis = aabb.maxAxisIndex()
            val median = aabb.center[maxAxis]
            val leftPart = ArrayList<Hitable>()
            val rightPart = ArrayList<Hitable>()
            for (hitable in hitables) {
                if (hitable.aabb().center[maxAxis] < median) {
                    leftPart.add(hitable)
                } else {
                    rightPart.add(hitable)
                }
            }
            left = if (leftPart.isNotEmpty()) BvhNode(leftPart) else null
            right = if (rightPart.isNotEmpty()) BvhNode(rightPart) else null
        } else {
            children = hitables.toTypedArray()
            left = null
            right = null
        }
    }

    override fun aabb(): Aabb {
        return aabb
    }

    override fun hit(ray: Ray, tMin: Float, tMax: Float): HitRecord? {
        if (!aabb.hit(ray, tMin, tMax)) {
            return null
        }
        if (children != null) {
            var result: HitRecord? = null
            var closest = tMax
            val to = children.size - 1
            for (i in 0..to) {
                val hitRecord = children[i].hit(ray, tMin, closest)
                if (hitRecord != null) {
                    result = hitRecord
                    closest = hitRecord.t
                }
            }
            return result
        } else {
            val leftHit = left?.hit(ray, tMin, tMax)
            val rightHit = right?.hit(ray, tMin, tMax)
            if (leftHit != null && rightHit != null) {
                return if (leftHit.t < rightHit.t) leftHit else rightHit
            } else if (leftHit != null) {
                return leftHit
            } else if (rightHit != null) {
                return rightHit
            } else {
                return null
            }
        }
    }

    private companion object {
        private const val BVH_THRESHOLD = 20
    }
}