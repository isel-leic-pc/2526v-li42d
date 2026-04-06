package pt.isel.pc.monitors

import kotlin.time.Duration

/**
 * The gate synchronizer
 * Must guarantee the all threads blocked
 * in "await" operation proceed after "open" operation
 */
class Gate {

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
    fun await(timeout: Duration) : Boolean {
        TODO()
    }
}