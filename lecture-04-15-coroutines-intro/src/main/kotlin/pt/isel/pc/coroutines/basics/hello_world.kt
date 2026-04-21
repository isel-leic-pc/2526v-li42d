package pt.isel.pc.coroutines.basics


import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine


private val logger = KotlinLogging.logger {}

suspend fun hello_msg() : String {

    suspendCoroutine<Unit> {
        cont ->
        logger.info("before  suspension")
        Thread {
            sleep(1000)
            cont.resumeWith(Result.success(Unit))
            logger.info("after first resume")
        }
        .start()

    }
    logger.info("after  suspension")

    return "Hello World!"
}

fun main() {

    val completion = object : Continuation<String> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<String>) {
            logger.info("on start completion")
            logger.info(result.getOrNull())
            logger.info("on end completion")
        }
    }

    // create the coroutine

    val routineContinuation :Continuation<Unit> = ::hello_msg.createCoroutine(completion)
    logger.info("coroutine created")

    // start the coroutine
    routineContinuation.resumeWith(Result.success(Unit))
    logger.info("coroutine started")

}