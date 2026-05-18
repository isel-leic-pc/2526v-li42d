package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutinesx.utils.getInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

class CoroutineScopeTests {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    suspend fun f1() {
        return withTimeout(10.seconds) {
            launch(CoroutineName("child1")) {
                logger.info("start child1")
                delay(4000)
                logger.info("end child1")
            }

            val child2 = launch(CoroutineName("child2")) {
                logger.info("start child2")
                delay(2000)
                logger.info("end child2")
            }

            val child3 = launch(CoroutineName("child3")) {
                launch {
                    delay(1000)
                    throw RuntimeException("oops!")
                }
            }

            delay(100)
            val coroutineMainJob = coroutineContext[Job]
            logger.info("coroutineMainJob before child3.join(): ${coroutineMainJob?.getInfo()}")

            try {
                child3.join()
            }
            catch(e : Exception) {
                logger.info("exception $e caught on child3.join()")
            }

            logger.info("coroutineMainJob after child3.join(): ${coroutineMainJob?.getInfo()}")

        }
    }

    @Test
    fun `check behaviour of a mailfunction coroutine inside a coroutineScope`() {
        runBlocking {
            logger.info("runBlocking coroutine Job: ${coroutineContext[Job]?.getInfo()}")
            try {
                f1();
            }
            catch(e: Exception) {
                logger.info("error $e caught in runBlocking")
            }
            logger.info("runBlocking job: ${coroutineContext[Job]?.getInfo()}")
        }
    }

    @Test
    fun `check withTimeout throwing exception`() {
        runBlocking(CoroutineName("Teste")) {
            logger.info("context on runBlocking is ${coroutineContext.getInfo()}")
            try {
                withTimeout(2.seconds) {
                    logger.info("context on withTimeout is ${coroutineContext.getInfo()}")
                    val child1 = launch {
                        delay(4000)
                    }

                    val child2 = launch {
                        delay(1000)
                    }

                    val mainTimeoutJob = coroutineContext[Job]
                    logger.info("before children termination, job is ${mainTimeoutJob?.getInfo()}")
                    try {
                        child1.join()
                        child2.join()
                    }
                    catch(e: Exception) {
                        logger.info("children join terminated with exceptionn $e")
                    }
                    logger.info("after childs termination, job is ${mainTimeoutJob?.getInfo()}")
                }
            } catch (e: TimeoutCancellationException) {
                logger.info("timeout cancellation exception catched")
            }
        }
    }



    @Test
    fun `check a cancel on a coroutine with an withTimeout scope in tandem`() {
        runBlocking {
            val child = launch() {
                try {
                    val res = withTimeout(2.seconds) {
                        delay(2000)
                    }
                }
                catch(e : CancellationException) {
                    logger.info("CancellationException catched")
                }
            }

            logger.info("Before cancelAndJoin")
            delay(1000)
            child.cancelAndJoin()
            logger.info("After cancelAndJoin")
        }
    }

    suspend fun f1(withError: Boolean = false) : String
    {
        return coroutineScope {
             val job = launch {
                logger.info("on child coroutine, ${coroutineContext.getInfo()}")
                delay(2000)
                if (withError)
                    throw RuntimeException("teste")
             }

            delay(500)
            try {
                delay(3000)
                //job.cancelAndJoin()
                logger.info("after cancelAndJoin, job= ${job.getInfo()}")
                logger.info("after cancelAndJoin, ctx= ${coroutineContext.getInfo()}")
            }
            catch(e: Exception) {
                logger.info("catch for $e")
                val myJob= coroutineContext[Job]
                logger.info("job state on catch is ${myJob?.getInfo()}")
                throw e
            }
            "Ok!"
        }
    }

    @Test
    fun `check coroutineScope cancellation doesn't cancel main coroutine`() {
        runBlocking {
            logger.info("${f1()}")
        }
    }

    @Test
    fun `check coroutineScope exception is propagate to main coroutine`() {
        runBlocking {
            try {
                logger.info("${f1(withError = true)}")
            }
            catch(e: Throwable) {
                logger.info("catch for $e")
                val myJob= coroutineContext[Job]
                logger.info("job state on catch is ${myJob?.getInfo()}")
            }
        }
    }

    @Test
    fun `check timeout doesn't cancel main coroutine 2`() {
        val scope = CoroutineScope(EmptyCoroutineContext)
        val cdl = CountDownLatch(1)
        scope.launch(CoroutineName("main")) {
            logger.info("${currentCoroutineContext()[Job]}")
            try {
                withTimeout(2.seconds) {

                    logger.info("before delay, job=${coroutineContext[Job]}")
                    coroutineContext.getInfo()
                    delay(4000)
                    logger.info("after delay, job=${coroutineContext[Job]}")
                }
            }
            catch(e: TimeoutCancellationException) {
                val job = coroutineContext[Job]
                logger.info("on timeout, job = $job, isCancelled=${job?.isCancelled}")

            }
            finally {
                cdl.countDown()
            }
        }

        cdl.await()
    }
}