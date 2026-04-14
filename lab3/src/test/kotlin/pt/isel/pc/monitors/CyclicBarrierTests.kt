package pt.isel.pc.monitors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CyclicBarrierTests {
    /**
     * test comment
     */
    @Test
    fun `cyclic barrier stress test`() {

        val PARTICIPANTS = 100
        val NROUNDS = 200
        val barrier = CyclicBarrier(PARTICIPANTS)
        val rounds = Array<Int>(PARTICIPANTS) { 0}

        val terminateLatch = CountDownLatch(PARTICIPANTS)

        val threads = (0 until PARTICIPANTS)
                      .map { index ->
                          Thread {
                              repeat(NROUNDS)  { round ->
                                  if (barrier.await(50.toDuration(DurationUnit.MILLISECONDS))) {
                                      rounds[index]++
                                  }
                                  sleep(Random.nextLong(10,20))
                              }
                              terminateLatch.countDown()
                          }
                      }
        threads.forEach {
            t -> t.start()
        }

        terminateLatch.await(8000, TimeUnit.MILLISECONDS)

        threads.forEachIndexed {
            index, t ->
            assertTrue(t.state == Thread.State.TERMINATED, "Thread $index not terminated")
        }

        for(i in (0 until PARTICIPANTS)) {
            assertEquals(NROUNDS, rounds[i], "error on index $i" )
        }
    }
}