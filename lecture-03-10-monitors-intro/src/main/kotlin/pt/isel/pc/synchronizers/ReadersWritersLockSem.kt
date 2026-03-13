package pt.isel.pc.synchronizers

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Just a try to present a possible pattern to
 * build generic synchronizers, including:
 *
 * the synchronization state
 * a mutex protecting the synchronization state concurrent access
 * a place where blocking threads that can't proceed immediately
 * In this case a semaphore is used for this purpose, but this has generally
 * severe consequences (eg: semaphore counter may not reflect the synchronization state,
 * lost notifications due the unavoidable window between the mutex unlock and semaphore acquire)
 *
 *
 * In this example we are trying to build a readers/writers lock giving priority to readers
 * (that is, while readers are in place, new entering readers are favored relative to new entering writers)
 * This is generally not the best choice (since it disable in time updates)
 *
 * This examples seems to work but has many (non-obvious) problems (eg: readers that could enter may
 * remain blocked in the semaphore).
 *
 * We neet a better model. And it exists in the form of the "monitor" concept, that we are going to study!
 */
class ReadersWritersLockSem {
    private val mutex = ReentrantLock()
    private val canAccess = Semaphore(0)

    // synchronizer state
    var writing = false
    var nReaders = 0


    fun enterRead() {
        mutex.lock()
        while(writing) {
            mutex.unlock()
            // Bad (unavoidable) Window!
            canAccess.acquire()
            mutex.lock()
        }
        nReaders++
        mutex.unlock()

    }

    fun enterWrite() {
        mutex.lock()
        while(nReaders > 0 || writing) {
            mutex.unlock()
            // Bad (unavoidable) Window!
            canAccess.acquire()
            mutex.lock()
        }
        writing = true
        mutex.unlock()
    }

    fun leaveRead() {
       mutex.withLock {
           nReaders--
           if (nReaders == 0) {
               canAccess.release()
           }
       }
    }

    fun leaveWrite() {
       mutex.withLock {
           writing = false
           canAccess.release()
       }
    }
}