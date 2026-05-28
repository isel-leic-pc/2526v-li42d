package pt.isel.pc.coroutines.structured_concurrency

import kotlinx.coroutines.*
import mu.KotlinLogging
import utils.getInfo
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

private val logger = KotlinLogging.logger {}

suspend fun inc(value : Int) : Int {
    try {
        delay(1000)
        logger.info("throw error")
        throw RuntimeException("bad calculation")
        return value + 1
    }
    catch(e: CancellationException)  {
        logger.info("cancellation catch on inc")
        throw e
    }

}

private fun longRun() {
    var sum: Long = 0
    val startTime = System.currentTimeMillis()
    for (i in 0..<Int.MAX_VALUE) {
        for (j in 0..10) {
            sum += j
        }
    }
}

suspend fun square(value : Int) : Int {
    try {
        // delay(3000)
        longRun()
        logger.info("square end")
        return value * value
    }
    catch(e: CancellationException) {
        logger.info("cancellation catch on square")
        throw e
    }
}

suspend fun call2SuspendInParallel(value : Int) : Int {
    val scope = CoroutineScope(EmptyCoroutineContext)
    logger.info("scope job is ${scope.coroutineContext[Job]?.getInfo()}")
    val job1 = scope.async {
        inc(value)
    }

    val job2 = scope.async {
        square(value)
    }

    return job1.await() + job2.await()
}

suspend fun call2SuspendInParallelUsingCoroutineScope(value : Int) : Int {
    return coroutineScope() {
        logger.info("scope job is ${coroutineContext[Job]?.getInfo()}")
        try {
            val job1 = async {
                inc(value)
            }

            val job2 = async {
                square(value)
            }
            val res = job1.await() + job2.await()
            logger.info("after awaits")
            res
        }
        catch(e: Exception) {
            logger.info("$e thrown on call2SuspendInParallelUsingCoroutineScope")
            throw e
        }
    }

}