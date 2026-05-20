package pt.isel.pc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.coroutines.EmptyCoroutineContext


class KotlinFlowTests {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    @Test
    fun `a simple flow test`() {

        val flow1 = flow<Int> {
            repeat(10) {
                logger.info("emit $it")
                emit(it)
                delay(1000)
            }
        }
        .map {
            logger.info("map emit ${it+1}")
            it+1
        }
        .flowOn(Dispatchers.Default)

        val result = mutableListOf<Int>()

        runBlocking {
            logger.info("start collect")
            flow1.collect {
                value ->
                result.add(value)
                logger.info("$value")
            }
        }

        val expected = List(10) { it+1 }
        assertEquals(expected, result)

    }

    @Test
    fun `flow with onFlow for different coroutine emission`() {

         val flow1 = flow<Int> {
            repeat(10) {
                logger.info("emit $it")
                emit(it)

                delay(1000)
            }
        }
        .map {
            logger.info("map emit ${it+1}")
            it+1
        }
        .flowOn(Dispatchers.Default)


        val result = mutableListOf<Int>()

        runBlocking {

            flow1.collect {
                    value ->
                result.add(value)
                logger.info("$value")
            }

        }
        val expected = List(10) { it+1 }

        assertEquals(expected, result)

    }

}