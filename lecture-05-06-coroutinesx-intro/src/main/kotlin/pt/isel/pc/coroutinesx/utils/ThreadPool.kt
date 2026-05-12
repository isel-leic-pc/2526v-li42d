package pt.isel.pc.coroutinesx.utils

import java.util.LinkedList
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

class ThreadPool(
    private val minThreadPoolSize: Int,
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration,
) {


    internal enum class PoolState {
        RUNNING, IN_SHUTDOWN, TERMINATED
    }

    private val mutex = ReentrantLock()
    private val workItems = LinkedList<Continuation<Unit>>()
    private val hasWorkItems = mutex.newCondition()
    private val terminated = mutex.newCondition()
    private var state = PoolState.RUNNING
    private var threadsCount = 0

    init {
        require (
            minThreadPoolSize > 0 &&
                    maxThreadPoolSize > 0 &&
                    minThreadPoolSize <= maxThreadPoolSize
        )
        mutex.withLock {
            repeat(minThreadPoolSize) {
                threadsCount = minThreadPoolSize
                thread(name =("ThreadPoolThread_$threadsCount")) {
                    workerLoop(null)
                }

            }
        }

    }

    private fun safeExec(continuation: Continuation<Unit>) {
        try {
            continuation.resume(Unit)
        }
        catch(e: Throwable) {
            // possibly logged error
        }
    }

    private sealed interface Result {
        data class WorkItem(val cont: Continuation<Unit>) : Result
        data object Timeout : Result
        data object InShutdown : Result
    }

    private fun  getWorkItem() : Result{
        // fast path
        if (workItems.isNotEmpty()) {
            return Result.WorkItem(workItems.removeFirst())
        }
        if (state == PoolState.IN_SHUTDOWN) {
            return Result.InShutdown
        }
        // Wait path
        var remainingNanos = keepAliveTime.inWholeNanoseconds
        while(true){
            remainingNanos = hasWorkItems.awaitNanos(remainingNanos)
            if (workItems.isNotEmpty()) {
                return Result.WorkItem(workItems.removeFirst())
            }
            if (state == PoolState.IN_SHUTDOWN) {
                return Result.InShutdown
            }
            if (remainingNanos <= 0) {
                return Result.Timeout
            }
        }
    }

    private fun threadTermination() {
        threadsCount--
        if (threadsCount == 0 && state == PoolState.IN_SHUTDOWN ) {
            state = PoolState.TERMINATED
            terminated.signalAll()
        }
    }

    private fun workerLoop(continuation: Continuation<Unit>?) {
        var currentCont = continuation
        while(true) {
            currentCont ?. apply { safeExec(currentCont) }
            mutex.withLock {
                currentCont = when(val res = getWorkItem()) {
                    is Result.WorkItem -> res.cont
                    is Result.Timeout -> {
                        threadTermination()
                        return
                    }

                    is Result.InShutdown -> {
                        threadTermination()
                        return
                    }
                }

            }
        }
    }

    @Throws(RejectedExecutionException::class)
    fun execute(continuation: Continuation<Unit>) {
        mutex.withLock {
            if (state != PoolState.RUNNING) throw RejectedExecutionException()
            if (threadsCount == maxThreadPoolSize) {
                workItems.add(continuation)
                hasWorkItems.signal()
            }
            else {
                threadsCount++
                thread(name="ThreadPool_$threadsCount") {
                    workerLoop(continuation)
                }

            }
        }
    }


    fun shutdown(): Unit {
        mutex.withLock {
            if (state == PoolState.RUNNING) {
                state = PoolState.IN_SHUTDOWN
                hasWorkItems.signalAll()
            }
        }
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout: Duration): Boolean {
        mutex.withLock {
            var timeoutNanos = timeout.inWholeNanoseconds
            while (state != PoolState.TERMINATED)  {
                if (timeoutNanos <= 0) return false
                timeoutNanos = terminated.awaitNanos(timeoutNanos)
            }
        }
        return true
    }
}

suspend fun <R> ThreadPool.invoke(f: () -> R): R {
    return suspendCoroutine {
            cont : Continuation<R>->

        val tpCont = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                cont.resume(f())
            }
        }
        try {
            execute(tpCont)
        }
        catch(e : RejectedExecutionException) {
            cont.resumeWith(Result.failure(e))
        }
    }
}

suspend fun ThreadPool.yield( )  {
    suspendCoroutine {
        cont ->
        execute(cont)
    }
}

