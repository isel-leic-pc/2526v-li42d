package pt.isel.pc.coroutines0

import java.util.concurrent.RejectedExecutionException
import kotlin.coroutines.Continuation
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


        fun execute(continuation: Continuation<Unit>): Unit {

        }

    }
}