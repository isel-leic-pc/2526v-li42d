package pt.isel.pc.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.Test

import pt.isel.pc.coroutines.utils.getInfo
import kotlin.time.Duration.Companion.seconds

class CoroutineScopeTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // using coroutineScope to create an inner scope
    suspend private fun f1() {
        //val scope = CoroutineScope(EmptyCoroutineContext)
       coroutineScope {
            launch {
                logger.info("start child1")
                delay(3000)
                logger.info("end child1")
            }

            launch {
                logger.info("start child2")
                delay(1000)
                logger.info("end child2")
            }
            launch {
                logger.info("start child3")
                delay(5000)
                logger.info("end child3")
            }
        }
        logger.info("done")
    }
    @Test
    fun `simple coroutine scope test`() {
        runBlocking {
            logger.info("start main")
            f1()
            logger.info("end main")
        }
    }

    // no using a corroutineScope with modified context (withContext)
    suspend private fun f2() {

        withContext(CoroutineName("withContext Name")) {
            logger.info("withContext context = ${coroutineContext.getInfo()}")
            launch {
                logger.info("start child1")
                delay(3000)
                logger.info("end child1")
            }

            launch {
                logger.info("start child2")
                delay(1000)
                logger.info("end child2")
            }
            launch {
                logger.info("start child3")
                delay(5000)
                logger.info("end child3")
            }
        }
        logger.info("done")
    }

    @Test
    fun `simple withContext test`() {
        runBlocking {
            logger.info("start main")
            f2()
            logger.info("end main")
        }
    }

    @Test
    fun `simple withTimeout test`() {
        runBlocking {
            logger.info("start main")
            try {
                withTimeout(2.seconds) {
                    delay(4000)
                }
            }
            catch(e: TimeoutCancellationException) {
                logger.info("timeout catched")
            }
            logger.info("end main")
        }
    }
}