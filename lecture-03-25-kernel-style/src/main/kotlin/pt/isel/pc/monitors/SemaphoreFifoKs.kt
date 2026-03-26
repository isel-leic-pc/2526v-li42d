package pt.isel.pc.monitors


import java.util.LinkedList
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Generic Semaphore with fair acquire (FIFO)
 * using kernel style technic with specific notification
 *
 */
class SemaphoreFifoKs(initialPermits: Int) {
    private val mutex = ReentrantLock()
    private var permits = initialPermits

    // used on non-specific notification solution
    // private val hasPermits = mutex.newCondition()

    private class PendingAcquire(val units: Int,
                                 val cond: Condition,
                                 var done: Boolean = false)

    // wait queue needed by fair acquire
    private val pendingAcquires = LinkedList<PendingAcquire>()

    init {
        require(initialPermits >= 0)
    }

    private fun tryResolveAcquires() {
//      for support of non-specific notification solution
//      val initialSize = pendingAcquires.size
        while(pendingAcquires.isNotEmpty() &&
            permits >= pendingAcquires.first().units) {
            val pa = pendingAcquires.removeFirst()
            permits -= pa.units
            pa.cond.signal()
            pa.done = true
        }
//        not having a diferent condition per request
//        broadcast notification is needed!
//        if (initialSize != pendingAcquires.size) {
//            hasPermits.signalAll()
//        }

    }

    @Throws(InterruptedException::class)
    fun acquire(units: Int, timeout: Duration) : Boolean {
        mutex.withLock {
            // fast path
            if (pendingAcquires.isEmpty() && permits >= units) {
                permits-= units
                return true
            }
            if (Duration.ZERO == timeout) return false
            val myAcquire = PendingAcquire(units, mutex.newCondition())
            pendingAcquires.addLast(myAcquire)
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            try {
                while (true) {
                    timeoutNanos = myAcquire.cond.awaitNanos(timeoutNanos)
                    if (myAcquire.done) return true
                    if (timeoutNanos <= 0) {
                        pendingAcquires.remove(myAcquire)
                        tryResolveAcquires()
                        return false
                    }
                }
            }
            catch(e: InterruptedException) {
                if (myAcquire.done) {
                    Thread.currentThread().interrupt()
                    return true
                }
                pendingAcquires.remove(myAcquire)
                tryResolveAcquires()
                throw e
            }

        }
    }

    fun release(units: Int) {
        mutex.withLock {
            permits += units
            tryResolveAcquires()

        }
    }
}