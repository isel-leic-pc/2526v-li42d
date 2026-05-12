package pt.isel.pc.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import pt.isel.pc.coroutines.asynchronizers.SemaphoreCR0
import pt.isel.pc.coroutines.utils.getInfo
import kotlin.test.Test

class ContinuationResumeTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun f1() {

        withContext(Dispatchers.Default) {
            val sem = SemaphoreCR0(0)

            logger.info("on withContext, context=${coroutineContext.getInfo()}")
            val child1 = launch {
                logger.info("before child1 first acquire")
                sem.acquire(1)
                logger.info("before child1 second acquire")
                sem.acquire(1)
                logger.info("after child1 second acquire")
            }

            val child2 = launch {
                logger.info("before child2 acquire")
                sem.acquire(1)
                logger.info("before child2 acquire")
            }


            val child3 = launch {
                logger.info("child 3 before release")
                sem.release(2)
                logger.info("child 3 after release")
            }

            child3.join()
            sem.release(1)
            child1.join()

            child2.join()


            logger.info("terminate f1")
        }
    }

    @Test
    fun `continuation possessing a lock`() {
        runBlocking {
           f1()

        }
    }
}