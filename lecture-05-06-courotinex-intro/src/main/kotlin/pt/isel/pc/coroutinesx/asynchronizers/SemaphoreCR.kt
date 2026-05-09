package pt.isel.pc.coroutinesx.asynchronizers

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * initial version os SemaphoreCR
 */
class SemaphoreCR(initialUnits : Int) {
    val mutex = ReentrantLock  ()
    var currentPermits = initialUnits
    init {
        require(initialUnits >= 0)
    }
    class PendingAcquire(val units: Int, val cont: Continuation<Unit>)

    // list of pending acquire continuations
    val pendingAcquires = LinkedList<PendingAcquire>()

    suspend fun acquire(units: Int) {
         suspendCoroutine {
             cont ->
             mutex.withLock {
                 if (currentPermits >= units) {
                     currentPermits -= units
                     cont.resume(Unit)
                 }
                 else {
                     pendingAcquires.add(PendingAcquire(units, cont))
                 }
             }
         }
    }


    fun release(units: Int) {
        mutex.withLock {
            currentPermits+= units
            while(pendingAcquires.isNotEmpty() && pendingAcquires.first().units <= currentPermits) {
                val pa = pendingAcquires.removeFirst()
                currentPermits -= pa.units
                // best to do resume out of lock
                pa.cont.resume(Unit)
            }
        }
    }
}