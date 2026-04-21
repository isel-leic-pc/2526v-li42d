package pt.isel.pc.coroutines_base

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import pt.isel.pc.utils.CoroutineName
import pt.isel.pc.utils.getCoroutineNameFromContinuationContext
import pt.isel.pc.utils.my_delay
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine


class CoroutinesBaseTests {

    private val logger = KotlinLogging.logger {  }
    
    suspend fun f1(i1: Int, i2: Int) : Int {
        logger.info("f1 started")
        return i1 + i2
    }



    @Test
    fun `call a suspend function without suspensions`() {
        val completion = object: Continuation<Int>  {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                logger.info("on coroutine completion, res=${result.getOrThrow()}")
            }
        }

        val starter : suspend () -> Int =  {
            f1(2, 3)
        }

        val cont : Continuation<Unit> = starter.createCoroutine(completion)
        logger.info("coroutine created")

        cont.resumeWith(Result.success(Unit))
        logger.info("coroutine started")
    }

    @Test
    fun `call a suspend function without suspensions as a normal (non suspend) function`() {
        val completion = object: Continuation<Int>  {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                logger.info("on continuation completion, res = ${result.getOrNull()}")
            }
        }

        @Suppress("UNCHECKED_CAST")
        val f1Normal = ::f1 as   (Int, Int, Continuation<Int>) -> Any

        val res = f1Normal(2, 3, completion)

        logger.info("f1Normal returned $res")
    }


    var notResumedContinuation : Continuation<Unit>? = null

    suspend fun delay_forever(millis: Long) {
        suspendCoroutine<Unit> {

            cont ->
                 notResumedContinuation = cont
        }
    }

    suspend fun f2() {
        logger.info("start f2")
        delay_forever(3000)
        logger.info("end f2 after delay")
    }



    @Test
    fun `call a suspend function with a suspension point`() {

        val completion = object: Continuation<Unit>  {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                logger.info("on continuation res = ${result.getOrNull()}")
            }
        }

        val cont = ::f2.createCoroutine(completion)

        logger.info("coroutine created")
        cont.resume((Unit))
        logger.info("coroutine started")

        //notResumedContinuation?.resume(Unit)
        assertNotNull(notResumedContinuation)
    }

    @Test
    fun `call a suspend function with a suspension point as a normal (non suspend) function`() {

        val completion = object: Continuation<Unit>  {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                logger.info("on continuation res = ${result.getOrNull()}")

            }
        }

        @Suppress("UNCHECKED_CAST")
        val f2Normal = ::f2 as (Continuation<Unit>) -> Any

        val res = f2Normal(completion)

        logger.info("f2Normal returned $res")
        // notResumedContinuation?.resume(Unit)

    }


    @Test
    fun `two coroutines simultaneously working on same thread`() {

        val continuations = ArrayDeque<Continuation<Unit>>()

        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
               logger.info("result = $result")
            }
        }

        val lambda1 : suspend () -> Unit = {
            var teams = listOf("Benfica", "Real Madrid", "Manchester United")
            for (t in teams) {
                print("$t: ")
                //logger.info("$t: ")
                suspendCoroutine<Unit> {
                    cont ->
                        continuations.addLast(cont)
                }
            }
        }

        val lambda2 : suspend () -> Unit = {
            var countries = listOf("Portugal", "Spain", "England")
            for (c in countries) {
                println(c)
                //logger.info(c)
                suspendCoroutine<Unit> {
                        cont : Continuation<Unit> ->
                            continuations.addLast(cont)
                }

            }
        }

        lambda1.startCoroutine(completion)
        logger.info("coroutine1 started")
        lambda2.startCoroutine(completion)
        logger.info("coroutine2 started")


        while(continuations.isNotEmpty()) {
            continuations.removeFirst().resumeWith(Result.success(Unit))
        }

        logger.info("done")
    }


}