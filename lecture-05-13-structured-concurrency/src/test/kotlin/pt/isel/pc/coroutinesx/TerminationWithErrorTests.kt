package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutinesx.utils.getInfo
import kotlin.time.Duration.Companion.seconds


class TerminationWithErrorTests {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    @Test
    fun `a throw on a child that blow up all coroutine tree 0`() {
        runBlocking {
            val jobParent = coroutineContext[Job]

            val child1 = launch(CoroutineName("child1")) {
                launch(CoroutineName("child2"))  {
                    logger.info("start child2")
                    delay(2000)
                    logger.info("end child 2")
                }

                launch(CoroutineName("child3"))  {
                    launch(CoroutineName("child4")) {
                        logger.info("start child4")
                        delay(2000)
                        logger.info("end child4")
                    }

                    delay(1000)
                    throw RuntimeException("oops!")
                }
            }

            launch((CoroutineName("child5"))) {
                launch((CoroutineName("child6"))) {
                    logger.info("start child6")
                    delay(5000)
                    logger.info("end child6")
                }
            }
            delay(100)
            try {
                logger.info("jobParent before join child: ${jobParent?.getInfo()}")
                child1.join()
            }
            catch(e: Exception) {
                logger.info("exception $e caught on runBlocking")
            }
            logger.info("jobParent after child completion:${jobParent?.getInfo()}")


            /**
             * refactor 1
            withContext(NonCancellable) {
                logger.info("start delay")
                delay(2000)
                logger.info("end delay")
            }
            */

            withContext(NonCancellable) {
                logger.info("start delay")
                delay(2000)
                logger.info("end delay")
            }

        }
    }

    /**
     *  check the effect an error terminated coroutine on coroutine scope function
     *  In this case withTimeout  but coroutineScope or withContext should be the saame
     */
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
    fun `a throw on a child that blow up all coroutine tree`() {
        val parent = SupervisorJob()
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            logger.info("exception $throwable caught at parent level")
        }
        /**
            refactor 2
            Using SupervisorJob and CoroutineExceptionHandler
            val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
                logger.info("exception $throwable caught at parent level")
            }
         */

        logger.info("${parent.getInfo()}")
        val scope = CoroutineScope(parent + exceptionHandler)
        logger.info("$scope")

        logger.info("${scope.coroutineContext.getInfo()}")

        val child4 = scope.launch(CoroutineName("child1")) {
            launch(CoroutineName("child2"))  {
                logger.info("start child2")
                delay(2000)
                logger.info("end child 2")
            }

            launch(CoroutineName("child3"))  {
                launch(CoroutineName("child4")) {
                    logger.info("start child4")
                    delay(2000)
                    logger.info("end child4")
                }

                delay(1000)
                throw RuntimeException("oops!")
            }
        }

        scope.launch((CoroutineName("child5"))) {
            launch((CoroutineName("child6"))) {
                logger.info("start child6")
                delay(5000)
                logger.info("end child6")
            }
        }


        runBlocking {
            delay(100)
            logger.info("parent before join =${parent.getInfo()}")

            /**
             *   refactor 3
             *   add parent.complete()
             *   parent.complete()
             */

            try {
                //parent.complete()
                child4.join()
            }
            catch(e: Exception) {
                logger.info("exception $e caught on parent.join()")
            }
            logger.info("jobParent after parent completion:${parent.getInfo()}")

        }
    }
}