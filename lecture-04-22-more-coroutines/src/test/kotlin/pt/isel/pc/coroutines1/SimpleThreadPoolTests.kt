package pt.isel.pc.coroutines1

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Thread.yield
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SimpleThreadPoolTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }



}