package pt.isel.pc.mm.lazy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import pt.isel.pc.mm.Lazy0
import pt.isel.pc.mm.Lazy1
import pt.isel.pc.mm.Lazy2
import pt.isel.pc.mm.LazyBuilder
import java.util.concurrent.atomic.AtomicInteger

class LazyTests {

    /**
     * lazy implementations sources in
     * https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/src/kotlin/util/Lazy.kt
     * https://github.com/JetBrains/kotlin/blob/2.3.20/libraries/stdlib/jvm/src/kotlin/util/LazyJVM.kt#L26
     */
    @Test
    fun `kotlin lazy sample`() {
        val answer: Int by lazy {
            println("Computing the answer to the Ultimate Question of Life, the Universe, and Everything")
            42
        }

        println("What is the answer?")
// Will print 'Computing...' and then 42
        println(answer) // 42
        println("Come again?")
// Will just print 42
        println(answer) // 42
    }

    val NTHREADS = 100
    val VALUE = 2L

    class Value<T>(val value : T) {
        companion object {
            val instancesCount = AtomicInteger()
        }
        init {
            instancesCount.incrementAndGet()
        }
    }

    private fun lazyTest(lazy: LazyBuilder<Value<Long>>) {
        val values = LongArray(NTHREADS)
        val threads = (0 until NTHREADS).map { index->
            Thread {
                values[index] = lazy.get().value
            }
        }
        for ( t in threads) {
            t.start()
        }
        for ( t in threads) {
            t.join(2000)
            assertFalse(t.isAlive)
        }
        for(v in values) {
            assertEquals(VALUE, v)
        }
        assertEquals(1, Value.instancesCount.get())
    }

    @Test
    fun `Lazy0 method correction test`() {
        val lazy = Lazy0 { Value(VALUE) }
        lazyTest(lazy)

    }

    @Test
    fun `Lazy1 method correction test`() {
        val lazy = Lazy1 { Value(VALUE) }
        lazyTest(lazy)
    }

    @Test
    fun `Lazy2 method correction test`() {
        val lazy = Lazy2 { Value(VALUE) }
        lazyTest(lazy)
    }

    @Test
    fun `kotlin  method correction test`() {
        val NTHREADS = 100
        val VALUE = 2L

        val  lazy : Value<Long>  by lazy {
            Value(VALUE)
        }
        val values = LongArray(NTHREADS)
        val threads = (0 until NTHREADS).map { index->
            Thread {
                values[index] = lazy.value
            }
        }
        for ( t in threads) {
            t.start()
        }
        for ( t in threads) {
            t.join(2000)
            assertFalse(t.isAlive)
        }
        for(v in values) {
            assertEquals(VALUE, v)
        }
        assertEquals(1, Value.instancesCount.get())
    }
}