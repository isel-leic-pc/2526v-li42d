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
 * that resumes out of the lock
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

               // Just ignore "invokeOnCancellation", since we resolve everything on CancellationException catch.
               //
               // This has a consequence. Even if the suspension state is terminated by cancellation,
               // a later release can alter this acquire to return with success, even if the coroutine is already canceled.
               // This happens when resume occurs before enter the lock in handleCancellation, on the catch of CancellationException.
               // But if we want cancellation to win in this case, we have to use a refactored "invokeOnCancellation".
               // We must change the flag "done" with a state that defines the acquire completion.
               // Something like enum class PendingState { Pending, Completed, Canceled }. On invokeOnCancellation,
               // if it is not already completed, we complete with state "Canceled" and remove from the "pendingAcquires" list.
               // on resume,  we complete with state "Completed". and remove from the "pendingAcquires" list.
               // this marks who win the race from our viewpoint.
               // But the viewpoint od the coroutine may be different. If  cancellation wins,
               // the CancellationException catch will call "handleCancellation", and in there we must decide
               // chacking the "pendingAcquire" state
               // Here we don't follow this path and don't use "invokeOnCancellation"

               //      cont.invokeOnCancellation {
               //         mutex.withLock {
               //            pendingAcquire?.done = true
               //            pendingAcquires.remove(pendingAcquire)
               //         }
               //     }

               mutex.withLock {
                   if (currentPermits >= units) {
                       currentPermits -= units
                       // this resume within the lock is not dangerous
                       // since it is just a flag for function internal state,
                       // telling that the function will  return synchronous
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

    // acquire version with timeout
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
        // best to do resumes out of lock in order to avoid deadlocks if
        // the coroutine resumes synchronously
        for(pa in resolved) {
            pa.cont.resume(Unit)
        }
    }
}