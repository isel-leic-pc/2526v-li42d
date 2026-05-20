package pt.isel.pc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test

class MyFlowTests {
    companion object {
        private   val logger = KotlinLogging.logger {}
    }

    private suspend fun inc(value: Int) : Int {
        delay(1000)
        return value + 1
    }

    @Test
    fun `simple flow test using suspend function`() {
        val flow1 : MyFlow<Int> = myFlow {
            try {
                logger.debug("start flow1")
                repeat(10) {
                    logger.debug("start flow1 emmit $it")
                    this.emit(inc(it))
                    delay(1000)
                    logger.debug("end flow1 emmit $it")
                }
                logger.debug("end flow1")
            }
            catch(e: Exception) {
                logger.debug("exception $e pass on myFlow block")
                throw e
            }
        }
        .map {
            it +2
        }
        .take(3)

        println("before flow")
        runBlocking {
            logger.debug("start flow1 collect")

            flow1.collect {
                println(it)
            }

            flow1.collect(object : MyFlowCollector<Int> {
                override suspend fun emit(t: Int) {
                    println(t)
                }
            })

            logger.debug("end flow1 collect")
        }
    }
}