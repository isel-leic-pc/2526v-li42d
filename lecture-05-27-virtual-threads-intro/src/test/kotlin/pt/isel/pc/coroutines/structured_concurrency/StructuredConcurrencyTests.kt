package pt.isel.pc.coroutines.structured_concurrency

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutines.structured_concurrency.*


class StructuredConcurrencyTests {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    @Test
    fun `test a function creating a scope and using async builder to call two suspend function in parallel`() {
        runBlocking {
            logger.info("start test")
            try {
                // n*n + (n+1)
                val res = call2SuspendInParallel(3)
                assertEquals(13, res)
            }
            catch(e: Exception) {
                logger.info("exception $e at runBlocking")
                delay(2000)
            }
            logger.info("end test")
        }
    }

    @Test
    fun `test a function using coroutineScope to call two suspend function in parallel`() {

        runBlocking {
            logger.info("start test")
            try {
                // n*n + (n+1)
                val res = call2SuspendInParallelUsingCoroutineScope(3)
                assertEquals(13, res)
            }
            catch(e: Exception) {
                logger.info("exception $e at runBlocking")
                delay(2000)
            }
            logger.info("end test")
        }

    }


}