package pt.isel.pc.monitors

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration


/**
 * A countdown latch with timeout on "await" operation
 */
class CountDownLatch(initialCount : Int) {
    private val mutex = ReentrantLock()
    private val zeroReached: Condition = mutex.newCondition()

    // synchronization state
    private var count = initialCount

    /**
     * decrement the "count" (if it is not zero) and
     * awake all waiting threads in "await" operation if
     * zero is reached
     */
    fun countDown() {
        mutex.withLock {
            if (count > 0) {
                if (--count == 0) {
                    zeroReached.signalAll()
                }

            }
        }
    }

    /**
     * pattern ilustration for implementation of a timeout operation.
     */
    fun await(timeout: Duration) : Boolean {
        mutex.withLock {
            // fast path
            if (count == 0) return true
            // zero timeout means we are just trying to resolve
            // the operation immediately, quiting if not.
            if (timeout == Duration.ZERO) return false
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            do {
                // using awaitNanos simplify the maintenance of the
                // remaining timeout
                timeoutNanos = zeroReached.awaitNanos(timeoutNanos)
                if (count == 0) return true
                if (timeoutNanos <= 0) return false
            }
            while(true)
        }
    }
}