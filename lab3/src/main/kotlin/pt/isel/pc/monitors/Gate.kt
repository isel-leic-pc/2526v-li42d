package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A kernel style implementation for the gate synchronizer.
 * It must guarantee the all threads blocked
 * in "await" operation (because the gate is closed)
 * will proceed after "open" operation
 */
class Gate(initialState : Boolean = false) {

    private val mutex = ReentrantLock()
    private var opened = initialState

    private val openDone = mutex.newCondition()

    private class Batch(var done: Boolean = false)

    // a batch waiter object for awaiting threads
    private var batch = Batch()

    /*
     * when the gate is opened
     * all the waiters in the "await"
     * operation must proceed
     */
    fun open() {
        mutex.withLock {
            opened = true
            batch.done = true
            batch = Batch()
            openDone.signalAll()
        }
    }

    /**
     * When the gate is closed the callers of
     * the "await" blocks until the next "open" operation
     */
    fun close() {
        mutex.withLock {
            opened = false
        }
    }

    /**
     * if the gate is opened  proceed immediately,
     * otherwise blocks until the next open operation
     */
    @Throws(InterruptedException::class)
    fun await(timeout: Duration = Duration.INFINITE): Boolean {
        mutex.withLock {
            // fast path
            if (opened) return true
            if (timeout == Duration.ZERO) return false

            var timeoutNanos = timeout.inWholeNanoseconds
            val myBatch = batch

            try {
                while (true) {
                    timeoutNanos = openDone.awaitNanos(timeoutNanos)
                    if (myBatch.done) return true
                    if (timeoutNanos <= 0) return false
                }
            }
            catch(e: InterruptedException) {
                if (myBatch.done) {
                    Thread.currentThread().interrupt()
                    return true
                }
                throw e
            }
            return true
        }
    }
}