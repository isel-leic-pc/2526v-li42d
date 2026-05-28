// To access the *non-public* Continuation API
// ONLY for learning purposes

@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package pt.isel.pc.virtual_threads.sequences


import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import pt.isel.pc.virtual_threads.sequences.Sequence.*
import java.lang.Thread.sleep
import kotlin.system.measureTimeMillis

class LoomGeneratorTestsKotlin {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    @Test
    fun `my sequence test`() {
        val evens = Sequence {
            var curr = 2

            while (true) {
                yield(curr) // next pair number
                curr += 2
            }
        }

        val evensLimited = evens
            .limit(20)

        var iter = evensLimited.iterator()

        while(iter.hasNext()) {
            println("${iter.next()}")
            
        }

    }

    @Test
    fun `hanoi tower generator test`() {
        var hanoiSeq = Sequence {
            fun hanoi(n: Int, start: Char, end: Char, aux: Char) {
                if (n > 0) {
                    hanoi(n-1 , start, aux, end)
                    yield(start + " to " + end )
                    hanoi(n - 1, aux, end, start)
                }
            }

            hanoi(3, 'A', 'C', 'B')
        }


//        hanoiSeq.forEach {
//            println(it)
//        }
        /*
        var moves = iterate (1)  { n -> n +1}
                    .zip(hanoiSeq) { n, s -> Pair(n, s) }
        */

        hanoiSeq.forEach {
            println(it)
        }
        var count = 0
        val millis = measureTimeMillis {

            for (move in hanoiSeq) {
                count++
            }
        }
        assertEquals(16*1024*1024-1, count)
        println("hanoi tower generator with loom test done in $millis milliseconds")

    }

    @Test
    fun `hanoi tower generator with kotlin sequences test`() {

        val hanoiSeq = sequence <String> {

            suspend fun SequenceScope<String>.hanoi(n: Int, start: Char, end: Char, aux: Char) {
                if (n > 0) {
                    hanoi(n-1 , start, aux, end)
                    yield(start + " to " + end )
                    hanoi(n - 1, aux, end, start)
                }
            }
            hanoi(4, 'A', 'B', 'C')
        }


        var count = 0

        var millis = measureTimeMillis {
            for (move in hanoiSeq) {
                count++
            }
        }

        assertEquals(16*1024*1024-1, count)
        println("hanoi tower kotlin generator test done in $millis milliseconds")

    }

    @Test
    fun `hanoi tower generator with kotlin sequences test2`() {

        fun hanoi(n: Int, start: Char, end: Char, aux: Char): kotlin.sequences.Sequence<String> = sequence {
            if (n > 0) {
                yieldAll(hanoi(n - 1, start, aux, end))
                yield(start + " to " + end)
                yieldAll(hanoi(n - 1, aux, end, start))
            }
            yieldAll(sequenceOf())
        }

        val hanoiSeq = hanoi(24, 'A', 'B', 'C')
        var count = 0

        var millis = measureTimeMillis {
            for (move in hanoiSeq) {
                count++
            }
        }

        assertEquals(16 * 1024 * 1024 - 1, count)
        println("hanoi tower kotlin 2 generator test done in $millis milliseconds")
    }

    @Test
    fun `simple asynchronous generator on virtual threads`() {
        val builder = Thread.ofVirtual().name("sequencer", 1)
        val t = builder.start {
            val evens = Sequence {
                var curr = 2

                while (true) {
                    yield(curr) // next pair number
                    logger.info("start produce next value")
                    println("${Thread.currentThread()}")
                    //sleep(2000)

                    curr += 2
                    logger.info("end produce next value")
                    println("${Thread.currentThread()}")
                }
            }

            evens
            .filter {
                n -> n % 4 == 0
            }
            .limit(10)
            .forEach {
                println("$it on ${Thread.currentThread()}")
            }

        }

        t.join()

        logger.info("end of test")
    }
}



