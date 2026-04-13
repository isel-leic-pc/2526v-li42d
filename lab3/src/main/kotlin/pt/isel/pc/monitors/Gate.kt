package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * The gate synchronizer.
 * It must guarantee the all threads blocked
 * in "await" operation (because the gate is closed)
 * must proceed after "open" operation
 */
class Gate(initialState : Boolean = false) {

    /*
     * when the gate is opened
     * all the waiters in the "await"
     * operation must proceed
     */
    fun open() {
        TODO()
    }

    /**
     * When the gate is closed the callers of
     * the "await" blocks until the next "open" operation
     */
    fun close() {
       TODO()
    }

    /**
     * if the gate is opened  proceed immediately,
     * otherwise blocks until the next open operation
     */
    fun await(timeout: Duration = Duration.INFINITE) : Boolean {
        TODO()
    }
}