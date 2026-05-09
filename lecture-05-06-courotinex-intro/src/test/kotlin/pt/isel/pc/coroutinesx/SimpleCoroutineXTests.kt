package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutinesx.utils.MyCoroutineName
import pt.isel.pc.coroutinesx.utils.MyDispatcher
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SimpleCoroutineXTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Test
    fun `first builder test`() {
        logger.info("start of test")
        val scope = CoroutineScope(EmptyCoroutineContext)

        logger.info("scope context is ${scope.coroutineContext}")

        val job = scope.launch( CoroutineName("another name") + MyDispatcher()) {
            logger.info("on new coroutine, context is $coroutineContext")
            val myJob = coroutineContext[Job]

            println("main coroutine job is $myJob")
            launch {
                logger.info("start child coroutine ")
                delay(3000)
                logger.info("end child coroutine ")
            }

        }
        logger.info("created job is $job")
        logger.info("end of test")

        val cdl = CountDownLatch(1)
        scope.launch {
            logger.info("wait for main coroutine")
            job.join()
            logger.info("after wait for main coroutine")
            cdl.countDown()
        }

        cdl.await()
    }

}