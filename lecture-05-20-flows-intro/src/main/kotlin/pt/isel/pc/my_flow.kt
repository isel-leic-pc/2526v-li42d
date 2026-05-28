package pt.isel.pc

import kotlinx.coroutines.FlowPreview
import mu.KotlinLogging
import pt.isel.pc.exceptions.AbortMyFlowException
import kotlin.coroutines.cancellation.CancellationException

private   val logger = KotlinLogging.logger {}

// the two interfaces below (MyFlow w MyFlowCollector) ara analogous
// to the interfaces that support kotlin flows (Flow and FlowCollector)
interface MyFlow<T> {
    suspend fun collect( collector : MyFlowCollector<T>)
}

fun interface MyFlowCollector<T> {
    suspend fun emit(t : T)
}

/**
 * This extension method support using a Function instead
 * of an anonymous class to pass de FlowCollector implementation to Flow collect
 *
 * REALLY NOT NECESSARY IF MyFlowCollector is marked as a "fun" interface
 *
 * That is;
 *
 * use, for instance the sintax:
 *  flow1.collect {
 *      println(it)
 *  }
 *
 *  instead of:
 *
 *  flow1.collect(object : MyFlowCollector<Int> {
 *      override suspend fun emit(t: Int) {
 *         println(t)
 *      }
 *  })
 */
//suspend  fun <T> MyFlow<T>.collect( collector: suspend (T) -> Unit) {
//    collect(object: MyFlowCollector<T> {
//        override suspend fun emit(t : T ) : Unit {
//            collector(t)
//        }
//    })
//}

/**
 * The basic builder of flows, analogous to flow builder of kotlin flows
 */
fun <T> myFlow ( block : suspend MyFlowCollector<T>.() -> Unit) : MyFlow<T> =
    object: MyFlow<T> {
        override suspend fun collect( collector : MyFlowCollector<T>) {
            try {
                collector.block()
            }
            catch(e: Exception) {
                logger.debug("exception $e passed on flow builder")
                throw e
            }
        }
    }


/**
 * map operation of flows
 * Intermediate operations are implemented creating new flows that collect
 * the flow which they wrap
 */
fun <T,U> MyFlow<T>.map(mapper : suspend (T) -> U) : MyFlow<U> {
    return myFlow {

        logger.debug("start map collect")
        collect { t ->
            try {
                logger.debug("map  start emit")
                emit(mapper(t))
                logger.debug("map  end emit")
            }
            catch(e: Exception) {
                logger.debug("exception $e passed on map block")
                throw e
            }
        }
        logger.debug("end map collect")


    }
}

/**
 * The take is interesting because is a short-circuit operations,
 * that is an operation that abort the flow emission when a certain state is achieved.
 * As you see, this is done using exception that propagate to called
 * upper flows that is the previous flows in the flow chain
 */
fun <T> MyFlow<T>.take(limit: Int) : MyFlow<T> {
    return myFlow {

       var remaining = limit
       try {
            logger.debug("start take collect")
            collect { t ->
                logger.debug("take  start emit, remaining $remaining")
                if (remaining > 0) {
                    emit(t)
                }
                remaining--
                logger.debug("take  end emit, remaining $remaining")
                if (remaining == 0) {
                    // short-circuit
                    logger.debug("collect abort trowing exception on take")
                    throw AbortMyFlowException(this)
                }
            }
        }

        catch(e : AbortMyFlowException) {
            logger.debug("exception catch on take")
            if (e.owner !== this) {
                throw e
            }
        }
        logger.debug("end take collect")
    }
}