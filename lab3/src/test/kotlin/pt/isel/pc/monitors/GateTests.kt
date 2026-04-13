package pt.isel.pc.monitors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class GateTests {
    @Test
    fun `gate stress test`() {
        val gate = Gate()
        val NWAITERS = 4
        val OPEN_ROUNDS = 100
        val results = IntArray(NWAITERS) {0}
        val terminated = CountDownLatch(NWAITERS)

        val threads = (0 until NWAITERS).map {
            index ->
            Thread {
                repeat(OPEN_ROUNDS) {
                    gate.await()
                    results[index]++
                }
                terminated.countDown()
            }
        }

        threads.forEach {
            t -> t.start()
        }

        repeat(OPEN_ROUNDS) {
            sleep(40)
            gate.open()
            gate.close()
        }

        terminated.await(2000, TimeUnit.MILLISECONDS)

        threads.forEachIndexed {
                index, t ->
                assertTrue(t.state == Thread.State.TERMINATED, "Thread $index not terminated")
        }

        for(i in 0 until NWAITERS) {
            assertEquals(OPEN_ROUNDS, results[i], "thread $i with ${results[i]} rounds")
        }
    }

}