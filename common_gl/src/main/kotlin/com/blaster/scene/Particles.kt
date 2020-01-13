package com.blaster.scene

import com.blaster.techniques.PositionsProvider
import org.joml.Vector3f
import java.nio.ByteBuffer

open class Particle(origin: Vector3f) {
    val position = Vector3f(origin)
}

class Particles(
        private val max: Int,
        private val emitters: List<Vector3f>,
        private val emitterFunction: (emitter: Vector3f, particles: MutableList<Particle>) -> Unit,
        private val particleFunction: (particle: Particle) -> Boolean) : PositionsProvider {

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

    override fun flush(buffer: ByteBuffer) {
        buffer.rewind()
        val floats = buffer.asFloatBuffer()
        particles.forEachIndexed { index, particle ->
            if (index >= max) {
                return
            }
            floats.put(particle.position.x)
            floats.put(particle.position.y)
            floats.put(particle.position.z)
        }
    }

    override fun count() = particles.size
}