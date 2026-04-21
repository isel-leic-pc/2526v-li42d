package pt.isel.pc.utils

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger {}

// a special scheduler for delay temporizations
private val scheduler = Executors.newSingleThreadScheduledExecutor()

/**
 * a context for coroutines in order to give them a name
 */
class CoroutineName(val name: String) : CoroutineContext.Element {
    companion object Key: CoroutineContext.Key<CoroutineName>
    override val key: CoroutineContext.Key<*>
        get() = CoroutineName

    override fun toString(): String = "CoroutineName($name)"
}

/**
 * get CoroutineName context name , from the context of Continuation cont.
 * If CoroutineName not exit on Continuation context, return "None"
 */
fun getCoroutineNameFromContinuationContext(cont : Continuation<*> ) : String {
    return cont.context.get(CoroutineName)?.name ?: "None"
}


/**
 * provides a delay temporization for coroutines
 */
suspend fun my_delay(millis: Long) {
    suspendCoroutine<Unit> { cont ->
        // just to check a non suspend situation
        if (millis == 0L)
            cont.resume(Unit)
        else {
            logger.info("in delay start")

            scheduler.schedule({
                logger.info("in delay end")
                cont.resume(Unit)
                logger.info("after resume in delay end")
            }, millis, TimeUnit.MILLISECONDS)
        }
    }
}
