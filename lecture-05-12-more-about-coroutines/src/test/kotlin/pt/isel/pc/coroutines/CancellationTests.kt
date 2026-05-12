package pt.isel.pc.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import pt.isel.pc.coroutines.utils.getInfo
import pt.isel.pc.coroutines.utils.state
import kotlin.coroutines.coroutineContext
import pt.isel.pc.coroutines.asynchronizers.SemaphoreCR0
import kotlin.time.Duration.Companion.seconds

class CancellationTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    @Test
    fun `a very simple cancellation test`() {

        runBlocking {

            logger.info("context on main: ${coroutineContext.getInfo()}")
            val child = launch {
                logger.info("child start")
                delay(3000)
            }
            delay(1000)
            logger.info("child before cancel =${child.getInfo()}")
            child.cancel()
            logger.info("child after cancel =${child.getInfo()}")
            child.join()
            logger.info("child after join =${child.getInfo()}")
        }
    }

    @Test
    fun `a  simple cancellation test`() {

        runBlocking {
            var grandChild : Job? = null
            logger.info("context on main: ${coroutineContext.getInfo()}")
            val child = launch {
                grandChild = launch {
                    logger.info("grand child start")
                    delay(10000)
                    logger.info("grand child end")

                }
                logger.info("child start")
                delay(3000)
            }
            delay(1000)
            logger.info("child before cancel =${child.getInfo()}")
            child.cancel()

            logger.info("child after cancel =${child.state}")
            logger.info("grand child after cancel =${grandChild?.state}")
            child.join()
            logger.info("child after join =${child.state}")
            logger.info("grand child after join =${grandChild?.state}")
        }


    }

    @Test
    fun `a  simple Semaphore use`() {
        runBlocking {
            val sem = SemaphoreCR0(0)
            val child = launch {
                logger.info("start child")
                try {
                    sem.acquire(1, 3.seconds)
                }
                catch( e : TimeoutCancellationException) {
                    logger.info("timeout occurred")
                }
            }
            delay(1000)
            //child.cancel()
            child.join()
            logger.info("child after join= ${child.state}")
            logger.info("done")
        }
    }

















    @Test
     fun `simple cancellation test`() {
        runBlocking {
            val child = launch(Dispatchers.Default) {
                logger.info("on child, ${coroutineContext.getInfo()}")

                delay(3000)
            }

            delay(1000)
            child.cancel()
            logger.info("on main, after child cancel, ${child.getInfo()}")
            child.join()

            logger.info("on main, after child termination, ${child.getInfo()}")

         }
     }

    @Test
    fun `check cancellation of coroutine on semaphore acquire operation`() {

    }

    @Test
    fun `check a cancel on a coroutine with an withTimeout scope in tandem`() {
        TODO()
    }

    @Test
    fun `execute code after cancellation test`() {
       TODO()
    }
}