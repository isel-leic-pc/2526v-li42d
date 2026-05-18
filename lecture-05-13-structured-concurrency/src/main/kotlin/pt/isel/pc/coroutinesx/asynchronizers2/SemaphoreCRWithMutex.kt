package pt.isel.pc.coroutinesx.asynchronizers2

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.LinkedList
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * A cancellable Semaphore using a kotlin Mutex instead of a ReentrantLock.
 * This is done just for completion purposes. We suggest use the ReentrantLock,
 * since it doesn't have the Mutex restrictions we present here.
 * Note in this case we can't just use the withLock inside suspendCancellableCoroutine block,
 * because the block is not marked with suspend and Mutex lock is a suspend function.
 * Note that in the CancellationException catch we must use withContext(NonCancellable)
 * because the coroutine is already canceled and suspend functions call is not allowed in this
 * state without NonCancellable context.
 */
class SemaphoreCRWithMutex(initialUnits : Int) {
    val mutex = Mutex()
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

    // acquire without timeout
    suspend fun acquire(units: Int)  {
        var pendingAcquire: PendingAcquire? = null
        try {
            // fast path
            mutex.lock()
            if (currentPermits >= units) {
                currentPermits -= units
                mutex.unlock()
            }
            else {
                suspendCancellableCoroutine<Unit> {
                        cont ->

                    // if we use invokeOnCancellation a runBlocking
                    // scope need to be used since invokeOnCancellation is non suspend
                    // cancellation
                    cont.invokeOnCancellation {
                        runBlocking {
                            mutex.withLock {
                                // process cancellation here
                            }
                        }

                    }

                    pendingAcquire = PendingAcquire(units, cont)
                    pendingAcquires.add(pendingAcquire)
                    mutex.unlock()
                }
            }
        }
        catch(e: CancellationException) {
            withContext(NonCancellable) {
                handleCancellation(e, pendingAcquire!!)
            }
        }
    }

    /**
     * release is marked with suspend to make possible
     * the use of Mutex
     */
    suspend fun release(units: Int) {
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
        // resumes must be done out of lock
        for(pa in resolved) {
            pa.cont.resume(Unit)
        }
    }
}