package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * A first synchronizer example using the "monitor" concept
 * There is a subtle, but very important, difference comparing to the previous version
 * (the semaphore as a blocking mechanism was replaced by a "condition").
 * A condition is just a waiting list for threads that can proceed.
 * But the important thing is that it belongs to a lock (the condition factory).
 * Now the  mutex release and the lock in the condition, are a compound atomic operation!
 * No intervening operation on the monitor can occur, drastically simplifying the code analysis.
 *
 * A reader/writer lock with readers priority using a monitor
 */
class ReadersWritersLock {

    private val mutex = ReentrantLock()
    private val canAccess= mutex.newCondition()

    private var writing = false
    private var nReaders = 0

    fun enterRead() {
        mutex.withLock {
            while(writing) {
                canAccess.await()
            }
            nReaders++
        }
    }

    fun enterWrite() {
        mutex.withLock {
            while(nReaders > 0 || writing) {
                canAccess.await()
            }
        }
    }

    fun leaveRead() {
        mutex.withLock {
            nReaders--
            if (nReaders == 0) {
                // awakening an eventual writer
                canAccess.signal()
            }
        }
    }

    fun leaveWrite() {
        mutex.withLock {
            writing = false
            // awakening all (suspended writers and suspended readers)
            // this is necessary because we must try to enable all readers to proceed.
            // Note that this is not guaranteed, due to indetermination of who will
            // enter the mutex next (maybe is a writer that is now entering on the monitor
            // for the first time). This situation is called "barging" and we will study forms
            // of avoiding it.
            // We may create a better solution using independent wait conditions for readers and writers,
            // but this will not avoid the barging problem.
            canAccess.signalAll()
        }
    }
}