package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A single acquire/single release semaphore
 * as an illustration of proper cancellation treatment on monitors
 */
class Semaphore1(initialPermits : Int) {
    private val mutex = ReentrantLock()
    private var permits = initialPermits
    private val hasPermits = mutex.newCondition()

    init {
        require(initialPermits >= 0)
    }

    @Throws(InterruptedException::class)
    fun acquire(timeout: Duration) : Boolean {
        mutex.withLock {
            // fast path
            if (permits > 0) {
                permits--
                return true
            }
            if (timeout == Duration.ZERO) return false
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            try {
                do {
                    timeoutNanos = hasPermits.awaitNanos(timeoutNanos)
                    if (permits > 0) {
                        permits--
                        return true
                    }
                    if (timeoutNanos <= 0) {
                        return false
                    }
                } while (true)
            }
            catch(e: InterruptedException) {
                // maybe we are signaled in simultaneous with interruption
                // and in that case we need to transfer the signal to other
                // (other way the signal could be lost).
                if (permits > 0) {
                    hasPermits.signal()
                }
                throw e
            }
        }
    }

    fun release() {
       mutex.withLock {
           permits++
           // just awake one, since it his a single release
           hasPermits.signal()
       }
    }
}