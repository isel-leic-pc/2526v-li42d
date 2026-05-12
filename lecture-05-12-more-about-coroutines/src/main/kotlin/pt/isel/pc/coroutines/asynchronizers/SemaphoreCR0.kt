package pt.isel.pc.coroutines.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

/**
 * initial version os SemaphoreCR
 */
class SemaphoreCR0(initialUnits : Int) {
    val mutex = ReentrantLock  ()
    var currentPermits = initialUnits
    init {
        require(initialUnits >= 0)
    }
    class PendingAcquire(val units: Int,
                         val cont: CancellableContinuation<Unit>,
                        var done: Boolean = false)

    // list of pending acquire continuations
    val pendingAcquires = LinkedList<PendingAcquire>()

    suspend fun acquire(units: Int, timeout: Duration = Duration.INFINITE)  {
        var pendingAcquire: PendingAcquire? = null
        try {
           withTimeout(timeout) {
               suspendCancellableCoroutine<Unit> {
                   cont ->

                    // Just ignore invokeOnCancellation since we resolve everithing on CancellationException catch
                    //               cont.invokeOnCancellation {
                    //                   mutex.withLock {
                    //                       pendingAcquire?.done = true
                    //                       pendingAcquires.remove(pendingAcquire)
                    //                   }
                    //               }

                   mutex.withLock {
                       if (currentPermits >= units) {
                           currentPermits -= units
                           cont
                       } else {
                           pendingAcquire = PendingAcquire(units, cont)
                           pendingAcquires.add(pendingAcquire)
                       }
                   }
               }
           }

        }
        catch(e: TimeoutCancellationException ) {
            mutex.withLock {
                if (pendingAcquire?.done!!) return
                pendingAcquires.remove(pendingAcquire)
                throw e
            }
        }
        catch(e: CancellationException) {
           mutex.withLock {
               if (pendingAcquire?.done!!) return
               pendingAcquires.remove(pendingAcquire)
               throw e
           }
        }
    }

    fun release(units: Int) {
        val resolved = mutex.withLock {
            currentPermits+= units
            val list = mutableListOf<PendingAcquire>()
            while(pendingAcquires.isNotEmpty() &&
                  pendingAcquires.first().units <= currentPermits) {
                val pa = pendingAcquires.removeFirst()
                currentPermits -= pa.units
                pa.done = true
                list.add(pa)
            }
            list
        }
        // best to do resumes out of lock
        for(pa in resolved) {
            pa.cont.resume(Unit)
        }
    }
}