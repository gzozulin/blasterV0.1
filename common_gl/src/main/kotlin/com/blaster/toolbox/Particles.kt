package com.blaster.toolbox

import com.blaster.techniques.BillboardsProvider
import org.joml.Vector3f
import java.nio.FloatBuffer

open class Particle(origin: Vector3f) {
    val position = Vector3f(origin)
    var scale = 1f
    var transparency = 1f
}

class Particles(
        private val max: Int,
        private val emitters: List<Vector3f>,
        private val emitterFunction: (emitter: Vector3f, particles: MutableList<Particle>) -> Unit,
        private val particleFunction: (particle: Particle) -> Boolean) : BillboardsProvider {

    private val particles = mutableListOf<Particle>()

    fun tick() {
        emitters.forEach {
            if (particles.size < max) {
                emitterFunction.invoke(it, particles)
            }
        }
        val particlesIterator = particles.iterator()
        while (particlesIterator.hasNext()) {
            val isAlive = particleFunction.invoke(particlesIterator.next())
            if (!isAlive) {
                particlesIterator.remove()
            }
        }
    }

    override fun flushPositions(position: FloatBuffer) {
        position.rewind()
        particles.forEachIndexed { index, particle ->
            if (index >= max) {
                return
            }
            position.put(particle.position.x)
            position.put(particle.position.y)
            position.put(particle.position.z)
        }
    }

    override fun flushScale(scale: FloatBuffer) {
        scale.rewind()
        particles.forEachIndexed { index, particle ->
            if (index >= max) {
                return
            }
            scale.put(particle.scale)
        }
    }

    override fun flushTransparency(transparency: FloatBuffer) {
        transparency.rewind()
        particles.forEachIndexed { index, particle ->
            if (index >= max) {
                return
            }
            transparency.put(particle.transparency)
        }
    }

    override fun count() = particles.size
}