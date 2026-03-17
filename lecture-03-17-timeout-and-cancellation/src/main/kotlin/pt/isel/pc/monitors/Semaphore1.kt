package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

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
           hasPermits.signal()
       }
    }
}