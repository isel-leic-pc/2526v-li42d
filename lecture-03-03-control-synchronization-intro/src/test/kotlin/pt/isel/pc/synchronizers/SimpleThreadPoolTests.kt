package pt.isel.pc.synchronizers

import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class SimpleThreadPoolTests {
    @Test
    fun `simple work deliver and execution test`() {
        val pool = SimpleThreadPool.create()
        //val sem = Semaphore(0)

        val latch = CountDownLatch(1)

        pool.submit {
                sleep(100)
            println("hello from ${Thread.currentThread()}")
            //sem.release()
            latch.countDown()
        }
        //sem.acquire()
        latch.await()

    }


}