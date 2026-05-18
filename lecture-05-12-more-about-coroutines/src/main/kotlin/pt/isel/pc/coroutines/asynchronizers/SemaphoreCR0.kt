package pt.isel.pc.coroutines.asynchronizers

import pt.isel.pc.coroutines.asynchronizers.SemaphoreCR.PendingAcquire
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * initial version without support for cancellation
 * and resumes inside lock
 * (not good)
 */
class SemaphoreCR0(initialUnits : Int) {
    init {
        require(initialUnits >= 0)
    }
    private var permits = initialUnits
    private val lock = ReentrantLock()

    private class Request(val units: Int,
                          val cont : Continuation<Unit>)

    private val requests = LinkedList<Request>()

    suspend fun acquire(units: Int) {
        lock.lock()

        suspendCoroutine<Unit> {
            cont ->
            lock.withLock {
                if (permits >= units) {
                    // fast path
                    permits -= units
                    // this resume within the lock is not dangerous
                    // since it is just a marcar for internal state
                    // telling that it has just a synchronous return
                    cont.resume(Unit)
                } else {
                    // suspend path
                    requests.add(Request(units, cont))
                }
            }

        }

    }
    
    private fun tryResolveRequests() : List<Request> {
        val resolved = mutableListOf<Request>()
        while(requests.isNotEmpty() &&
            requests.first().units <= permits) {
            val r = requests.removeFirst()
            permits -= r.units
            resolved.add(r)
        }
        return resolved
    }

    suspend fun release(units: Int) {
        val resolved = lock.withLock {
            permits += units
            tryResolveRequests()
        }

        // resume resolved acquires
        for(r in resolved) {
            r.cont.resume(Unit)
        }
    }
}