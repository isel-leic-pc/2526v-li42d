package pt.isel.pc.coroutines.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * A cancellable version of SemaphoreCR
 * and resumes out of the lock
 * (better)
 */
class SemaphoreCR(initialUnits : Int,
                  // used to synchronize tests
                  val cdl : CountDownLatch = CountDownLatch(1)) {
    val mutex = ReentrantLock()
    var currentPermits = initialUnits

    init {
        require(initialUnits >= 0)
    }
    class PendingAcquire(val units: Int,
                         val cont: CancellableContinuation<Unit>,
                        var done: Boolean = false)

    // list of pending acquire continuations
    val pendingAcquires = LinkedList<PendingAcquire>()

    private suspend fun handleCancellation(e: Exception, pa: PendingAcquire) {
        mutex.withLock {
            if (pa.done) return
            pendingAcquires.remove(pa)
            throw e
        }
    }

    // acquire version without timeout
    suspend fun acquire(units: Int)  {
        var pendingAcquire: PendingAcquire? = null
        try {

           // use of cancellable suspension
           suspendCancellableCoroutine<Unit> {
               // cont is now a CancellableContinuation
               cont ->

               // Just ignore invokeOnCancellation since we resolve everything on CancellationException catch.
               // This has a consequence. Even if the suspension is terminated by cancellation,
               // a consequent release can alter this acquire to return with success,
               // even if coroutine is already canceled.
               // This can happen if resume happens before enter the lock in handleCancellation, called in
               // the catch of CancellationException.
               // But if we want cancellation to win in this case, we have to change flag done with a state that defines
               // the acquire completion.
               // Something like enum class PendingState { Pending, Completed, Canceled }
               // And check the state handleCancellation.
               // Here we don't follow this path and don't use invokeOnCancellation

//                   cont.invokeOnCancellation {
//                       mutex.withLock {
//                           pendingAcquire?.done = true
//                           pendingAcquires.remove(pendingAcquire)
//                       }
//                   }

               mutex.withLock {
                   if (currentPermits >= units) {
                       currentPermits -= units
                       // this resume within the lock is not dangerous
                       // since it is just a marcar for internal state
                       // telling that it has just a synchronous return
                       cont.resume(Unit)
                   } else {
                       pendingAcquire = PendingAcquire(units, cont)
                       pendingAcquires.add(pendingAcquire)
                   }
               }
           }

        }
        catch(e: CancellationException) {
            return handleCancellation(e, pendingAcquire!!)
        }
    }

    // acquire version with Timeout
    suspend fun acquire(units: Int, timeout: Duration) {
        withTimeout(timeout) {
            acquire(units)
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