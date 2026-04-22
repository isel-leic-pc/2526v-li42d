package pt.isel.pc.coroutines1

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.Unit
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.time.Duration

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
    private val workItems = LinkedList<Continuation<Unit>>()
    private val hasWorkItems = mutex.newCondition()

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

    private fun workerLoop(initialContinuation: Continuation<Unit>) {
        // resume received continuation
        var nextContinuation = initialContinuation
        while(true) {
            safeExec(nextContinuation)

            mutex.withLock {
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


