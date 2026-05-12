package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.KotlinLogging.logger
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutinesx.utils.MyDispatcher
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext



suspend fun test() {
    val ctx = currentCoroutineContext()
    println("in test, coroutinecontex is $ctx")
}

class CoroutineContextExamples {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `multiple coroutines on a new Scope`() {

        var scope = CoroutineScope(EmptyCoroutineContext);

        logger.info("start test")
        var cdl = CountDownLatch(1)
        scope.launch(EmptyCoroutineContext) {
            logger.info("start coroutine")
            println(coroutineContext)
            val job = coroutineContext[Job]

            println("job=$job")
            println("in parent coroutine $coroutineContext")
            launch {

                logger.info("in subCoroutine $coroutineContext")
                logger.info("start sub coroutine")
                delay(4000)  // what if it was sleep?
                logger.info("end sub coroutine")
            }
            logger.info("start delay")
            delay(2000) // what if it was sleep?
            logger.info("end coroutine")
            cdl.countDown()
        }

        cdl.await()
        logger.info("end test")
    }

    @Test
    fun `using assignment 2 ThreadPool as a coroutine dispatcher`() {
        val scope = CoroutineScope(MyDispatcher())

        println(scope.coroutineContext)
        val scopeJob = scope.coroutineContext[Job]

        val job = scope.launch {
            launch(MyDispatcher()) {
                logger.info("first scope coroutine start, context is ${currentCoroutineContext()}")
                delay(3000)
                logger.info("first scope coroutine end")
            }

            launch {
                logger.info("second scope coroutine start, context is ${currentCoroutineContext()}")
                delay(5000)
                logger.info("second scope coroutine end")
            }
        }



    }
}