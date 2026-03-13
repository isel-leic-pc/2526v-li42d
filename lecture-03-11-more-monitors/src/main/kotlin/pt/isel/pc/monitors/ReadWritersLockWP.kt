package pt.isel.pc.monitors

import java.security.AlgorithmParameters
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A version of ReadersWritersLock that gives priority to writers.
 * In general a better solution than "readers priority", since we want to guarantee
 * the changes are done "in time".
 */
class ReadWritersLockWP {
    private val mutex = ReentrantLock()

    // we used here two conditions, in order to discriminate
    // who we want to awake
    private val canAccessRead = mutex.newCondition()
    private val canAccessWrite = mutex.newCondition()

    // synchronization state
    private var writing = false
    private var nReaders = 0

    // the raeders can't proceed if there are
    // writers trying to proceed
    private var waitingWriters = 0

    fun enterRead() {
        mutex.withLock {
            while(writing || waitingWriters > 0) {
               canAccessRead.await()
            }
            nReaders++
        }
    }

    fun enterWrite() {
        mutex.withLock {
             while(writing || nReaders > 0) {
                 waitingWriters++
                 canAccessWrite.await()
                 waitingWriters--
             }
             writing = true
        }
    }

    fun leaveRead() {
        mutex.withLock {
            // change of synchronization state
            if (--nReaders == 0) {
                tryAwakePartners()
            }
        }
    }

    fun leaveWrite() {
        mutex.withLock {
            // change of synchronization state
            writing = false
            tryAwakePartners()
        }
    }

    private fun tryAwakePartners() {
        // priority to writers
        if (waitingWriters> 0)
            canAccessWrite.signal()
        else
            // readers cant all proceed.
            // Note this is not guaranteed due to barging.
            // We will solve this latter.
            canAccessRead.signalAll()
    }
}