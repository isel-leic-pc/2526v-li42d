package pt.isel.pc.coroutines1

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.Unit
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * A simple thread pool that executes continuations,
 * without controlled termination (no explicit shutdown).
 */
class SimpleThreadPool(
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration,
    ) {
    init {
        require (
            maxThreadPoolSize > 0
        )
    }

    private val mutex = ReentrantLock()
    private var threadsCount = 0

    // list of pending workItems (continuations to resume)
    private val workItems = LinkedList<Continuation<Unit>>()
    private val hasWorkItems = mutex.newCondition()

    /**
     * resume the continuation without causing
     * disruption to the pool thread.
     */
    private fun safeExec(cont : Continuation<Unit>) {
        try {
            cont.resume(Unit)
        }
        catch(e: Throwable) {
            // just ignore for now
        }
    }

    private sealed interface Result {
        class WorkItem(val cont: Continuation<Unit>) : Result
        object Timeout : Result
    }

    // must be called with mutex owned
    private fun terminateThread() {
        --threadsCount
    }

    // must be called with mutex owned
    private fun getNextWorkItem() : Result {
        // fast path
        if (workItems.isNotEmpty())
            return Result.WorkItem(workItems.removeFirst())
        // wait path
        var remainingTimeNanos = keepAliveTime.inWholeNanoseconds

        while(true) {
            remainingTimeNanos = hasWorkItems.awaitNanos(remainingTimeNanos)
            if (workItems.isNotEmpty()) return Result.WorkItem(workItems.removeFirst())
            if (remainingTimeNanos <= 0) return Result.Timeout
        }

    }

    /**
     * code for the pool worker threads
     */
    private fun workerLoop(initialContinuation: Continuation<Unit>) {
        // save received continuation
        var nextContinuation = initialContinuation
        while(true) {
            // resume continuation out of lock
            // this is essential to correct execution of the pool
            safeExec(nextContinuation)

            mutex.withLock {
                // get the result of retrieve a new workitem from the list
                // could be a Result.WorkItem or Result.Timeout
                nextContinuation = when(val res = getNextWorkItem()) {
                    is Result.WorkItem -> res.cont
                    is Result.Timeout -> {
                        terminateThread()
                        return
                    }
                }
            }
        }

    }


    /**
     * Function used to submit a Continuation to the pool
     * if the number of pool threads doesn't reach the maximum, a new thread
     * is created and the Continuation is directly send to it.
     * If not, it is saved in the workItems list to future resuming by a pool thread.
     */
    fun execute(continuation: Continuation<Unit>) {
        mutex.withLock {
            if (threadsCount == maxThreadPoolSize) {
                workItems.addLast(continuation)
                hasWorkItems.signal()
            }
            else { // threadsCount < maxThreadPoolSize
                threadsCount++
                Thread {
                    workerLoop(continuation)
                }
                .start()
            }
        }
    }

}

suspend fun <R> SimpleThreadPool.invoke(f: () -> R): R {
    TODO()
}


