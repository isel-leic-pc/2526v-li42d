package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration


class SemaphoreN(initialPermits : Int) {
    private val mutex = ReentrantLock()
    private var permits = initialPermits
    private val hasPermits = mutex.newCondition()

    init {
        require(initialPermits >= 0)
    }

    @Throws(InterruptedException::class)
    fun acquire(units: Int, timeout: Duration) : Boolean {
       mutex.withLock {
           // fast path
           if (permits >= units) {
               permits -= units
               return true
           }
           if (timeout == Duration.ZERO) return false
           // wait path
           var timeoutNanos = timeout.inWholeNanoseconds
           do {
               timeoutNanos = hasPermits.awaitNanos(timeoutNanos)
               if (permits >= units) {
                   permits -= units
                   return true
               }
               if (timeoutNanos <= 0) return false
           }
           while(true)
       }
    }

    fun release(units: Int) {
        mutex.withLock {
            permits += units
            hasPermits.signalAll()
        }
    }
}