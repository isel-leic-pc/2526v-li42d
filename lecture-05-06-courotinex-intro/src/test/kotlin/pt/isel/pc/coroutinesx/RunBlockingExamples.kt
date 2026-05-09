package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import pt.isel.pc.coroutinesx.utils.MyCoroutineName
import pt.isel.pc.coroutinesx.utils.MyDispatcher
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test

class RunBlockingExamples {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `simple RunBlocking`() {
        logger.info("start test")
        runBlocking {
            logger.info("start coroutine")
            delay(2000)
            logger.info("end coroutine")
        }
        logger.info("end test")
    }

    @Test
    fun `multiple coroutines on RunBlocking`() {
        logger.info("start test")
        runBlocking(EmptyCoroutineContext) {
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
        }
        logger.info("end test")
    }

    @Test
    fun `use threadpool as dispatcher`() {
        runBlocking(MyDispatcher()) {
            logger.info("in coroutine coroutineContext=${kotlin.coroutines.coroutineContext}")
        }
    }

    class MyTest : CoroutineContext.Element {
        companion object Key: CoroutineContext.Key<MyTest>
        override val key: CoroutineContext.Key<*>
            get() = MyTest

       // override fun toString(): String = "MyTest"
    }

    @Test
    fun `access a non existent context`() {
        runBlocking {
            val ctx = coroutineContext[Job]
            println(ctx)
        }
    }

    @Test
    fun `manually created scope`() {
       val scope = CoroutineScope(EmptyCoroutineContext)

       val job = scope.coroutineContext[Job]




    }
}