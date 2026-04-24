package pt.isel.pc.coroutines1

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Thread.yield
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SimpleThreadPoolTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    @Test
    fun `test with a simple continuation`() {
        val pool = SimpleThreadPool(10, 1.toDuration(DurationUnit.MINUTES))
        var res = 0
        val latch = CountDownLatch(1)

        val cont = object: Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                res++
                logger.info("cont resumed on pool")
                latch.countDown()
            }
        }

//      the commented code below illustrates the use of Continuation constructor function
//      that produces an implementation of Continuation interface analogous to the
//      one above.

//        val cont2 = Continuation<Unit>(EmptyCoroutineContext) {
//             res++
//             logger.info("cont resumed on pool")
//             latch.countDown()
//        }

        pool.execute(cont)

        latch.await(2000, TimeUnit.MILLISECONDS)
        logger.info("on test, get the result")
        assertEquals(1, res)

    }

    @Test
    fun `test with a simple continuation using invoke`() {
        val pool = SimpleThreadPool(10, 1.toDuration(DurationUnit.MINUTES))
        var res = 0
        val latch = CountDownLatch(1)

        val cont = Continuation<Unit>(EmptyCoroutineContext) {
            latch.countDown()
        }

        val corLambda : suspend () -> Unit = {
            res = pool.invoke {
                logger.info("on pool, result produced")
                res + 1
            }
        }
        corLambda.startCoroutine(cont)
        latch.await(2000, TimeUnit.MILLISECONDS)

        logger.info("on test, get the result")
        assertEquals(1, res)

    }



}