package pt.isel.pc.coroutinesx.utils

import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

// context element example
class MyCoroutineName(val name: String) : CoroutineContext.Element {
    companion object Key: CoroutineContext.Key<MyCoroutineName>
    override val key: CoroutineContext.Key<*>
        get() = MyCoroutineName

    override fun toString(): String = name
}

class MyDispatcher : ContinuationInterceptor {
    companion object Key: CoroutineContext.Key<MyDispatcher>

    val pool = ThreadPool(1, 8, 60.seconds)

    override val key: CoroutineContext.Key<*>
        get() = MyDispatcher


    override  fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return object : Continuation<T> {
            override val context: CoroutineContext
                get() = continuation.context

            override fun resumeWith(result: Result<T>) {
                pool.execute(continuation as Continuation<Unit>)
            }
        }
    }

    override fun toString(): String = "Dispatcher(MyDispatcher)"
}