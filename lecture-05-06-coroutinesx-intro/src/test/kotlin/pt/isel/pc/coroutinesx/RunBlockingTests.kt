package pt.isel.pc.coroutinesx

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

class RunBlockingTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `simple run blocking test`() {
        logger.info("test start")
        runBlocking {
            launch {
                logger.info("child coroutine context is ${currentCoroutineContext()}")
                delay(5000)
                logger.info("after delay on child coroutine")
            }
            logger.info("main coroutine context is ${currentCoroutineContext()}")
            delay(2000)
            logger.info("main coroutine context after delay")
        }
        logger.info("test end")
    }
}