package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A kernel style cyclic barrier is a synchronizer used on
 * scenarios where a set of worker threads execute a process in several steps.
 * Step i+1 can only be started in each of the worker threads when step i is done on all threads
 * (in this case with success or failure (by timeout or interruption)
 * There is just one operation: "await"
 * In order to synchronize execution all threads must call "await" operation on the end of each step.
 * "await" calls block the threads, except the last one, that "awakes" all remaining threads.
 */
class CyclicBarrier(val participants : Int) {
    private class Round(var done : Boolean = false)

    private var current = participants
    private val mutex = ReentrantLock()
    private val terminatedRound = mutex.newCondition()

    private var round = Round()

    @Throws(InterruptedException::class)
    fun await( timeout: Duration = Duration.INFINITE) : Boolean{
        mutex.withLock {
            // fast path
            if (current == 1) {
                round.done = true
                round = Round()
                terminatedRound.signalAll()
                current = participants
                return true
            }
            --current
            if (timeout == Duration.ZERO) return false

            // wait path
            val myRound = round
            var timeoutNanos = timeout.inWholeNanoseconds
            try {
                do {
                    timeoutNanos = terminatedRound.awaitNanos(timeoutNanos)
                    if (myRound.done) return true
                    if (timeoutNanos <= 0) return false
                } while (true)
            }
            catch(e: InterruptedException) {
                if (myRound.done) {
                    Thread.currentThread().interrupt()
                    return true
                }
                throw e
            }
        }
    }

}