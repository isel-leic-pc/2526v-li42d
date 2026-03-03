package pt.isel.pc.lab1.locks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.isel.pc.locks.Counter
import pt.isel.pc.locks.Counter2
import kotlin.concurrent.thread

class CounterTests {
    @Test
    fun `increment counter by multiple threads test`() {
        val counter = Counter()

        val NTHREADS = 20
        val NITERS = 1_000_000

        val threads : MutableList<Thread> =
            mutableListOf()

        repeat(NTHREADS) {
            val t = thread {
                repeat(NITERS) {
                    counter.inc()
                }
            }
            threads.add(t)
        }

        threads.forEach {
            it.join()
        }

        assertEquals((NTHREADS*NITERS).toLong(),counter.get() )
    }
}