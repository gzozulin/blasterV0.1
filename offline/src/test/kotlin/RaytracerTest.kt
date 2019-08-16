import com.blaster.math.AABB
import com.blaster.math.Ray
import com.blaster.math.Vec3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*
import kotlin.system.measureNanoTime

internal class RaytracerTest
{
    @Test
    fun shouldAabbComputeCorrectCenter()
    {
        val aabb = AABB(Vec3(0f), Vec3(2f))
        assertEquals(aabb.center, Vec3(1f))
    }

    @Test
    fun shouldAddAabbCorrectly()
    {
        val first = AABB(Vec3(0f), Vec3(1f))
        val second = AABB(Vec3(-1f), Vec3(2f))
        assertEquals(first + second, second)
        assertEquals(second + first, second)
    }

    @Test
    fun shouldFindMaxAxis0Index()
    {
        val aabb = AABB(Vec3(-10f, 0f, 0f), Vec3(10f, 3f, 2f))
        assertEquals(aabb.maxAxisIndex(), 0)
    }

    @Test
    fun shouldFindMaxAxis1Index()
    {
        val aabb = AABB(Vec3(0f), Vec3(3f, 5f, 3f))
        assertEquals(aabb.maxAxisIndex(), 1)
    }

    @Test
    fun shouldFindMaxAxis2Index()
    {
        val aabb = AABB(Vec3(0f), Vec3(1f, 2f, 3f))
        assertEquals(aabb.maxAxisIndex(), 2)
    }

    val random = Random()

    @Test
    fun thousandRayHits() {
        var nanotime = measureNanoTime {
            for (i in 1..100_000) { // warmup
                microbenchmark()
            }
            for (i in 1..50_000_000) {
                microbenchmark()
            }
        }
        println("done in ${nanotime / 1_000_000_000f}")
    }

    fun microbenchmark() {
        val aabb = AABB(
            Vec3(0f),
            Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat())
        )
        val rayOrigin = Vec3(-100f * random.nextFloat())
        val rayDirection = (aabb.center - rayOrigin).makeUnit()
        val ray = Ray(rayOrigin, rayDirection)
        assertTrue(aabb.hit(ray, Float.MIN_VALUE, Float.MAX_VALUE))
    }
}