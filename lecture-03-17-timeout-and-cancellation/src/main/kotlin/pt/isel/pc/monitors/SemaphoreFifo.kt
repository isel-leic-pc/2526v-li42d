package pt.isel.pc.monitors


import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SemaphoreFifo(initialPermits: Int) {
    private val mutex = ReentrantLock()
    private var permits = initialPermits
    private val hasPermits = mutex.newCondition()

    private class PendingAcquire(val units: Int)
    private val pendingAcquires = LinkedList<PendingAcquire>()

    init {
        require(initialPermits >= 0)
    }

    private fun trySignal() {
        if (pendingAcquires.isNotEmpty() && permits >= pendingAcquires.first().units ) {
            hasPermits.signalAll()
        }
    }

    @Throws(InterruptedException::class)
    fun acquire(units: Int, timeout: Duration) : Boolean {
        mutex.withLock {
            // fast path
            if (pendingAcquires.isEmpty() && permits >= units) {
                permits -= units
                return true
            }
            if (timeout == Duration.ZERO) return false
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            val myAcquire = PendingAcquire(units)
            pendingAcquires.addLast(myAcquire)
            try {
                do {
                    timeoutNanos = hasPermits.awaitNanos(timeoutNanos)
                    if (pendingAcquires.first() == myAcquire && permits >= units) {
                        pendingAcquires.removeFirst()
                        permits -= units
                        trySignal()
                        return true
                    }
                    if (timeoutNanos <= 0) {
                        pendingAcquires.remove(myAcquire)
                        trySignal()
                        return false
                    }
                }
                while(true)
            }
            catch(e: InterruptedException) {
                pendingAcquires.remove(myAcquire)
                trySignal()
                throw e
            }

        }
    }

    fun release(units: Int) {
        mutex.withLock {
            permits += units
            trySignal()
        }
    }
}