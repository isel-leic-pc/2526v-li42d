package pt.isel.pc

import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

class SequenceTests {
    companion object {
        private   val logger = KotlinLogging.logger {}
    }

    fun inc(value: Int) : Int {
        sleep(1000)
        return value + 1
    }

    @Test
    fun `simple sequence test`() {
        val seq = sequence<Int> {
            repeat(10) {
                println("produce $it")
                yield(inc(it))
            }
        }

        println("before sequence forEach")
        seq.forEach {
            println(it)
        }
    }

    @Test
    fun `can't invoke  suspend functions on sequence test`() {
        val seq = sequence<Int> {
            repeat(10) {
                // compilation error
                println("produce $it")
                yield(   inc(it))
            }
        }

        println("before sequence forEach")
        seq.forEach {
            println(it)
        }
    }



    @Test
    fun `test sequence production of hanoi tower movements`() {

            fun hanoi(nDisks: Int, start: Char, end: Char, aux: Char) : Sequence<String>  = sequence {
                if (nDisks > 0) {
                    //logger.info("start yieldAll 1")
                    yieldAll(hanoi(nDisks-1, start, aux, end ))
                    //
                    //logger.info("end yieldAll 1")
                    //logger.info("start yield '$start to $end'")
                    yield("$start to $end")
                    //logger.info("end yield '$start to $end'")
                    //logger.info("start yieldAll 2")
                    yieldAll(hanoi(nDisks-1, aux, end, start ))
                    //logger.info("end yieldAll 2")
                }
            }

            // it was  A, B, C, that why the final destination was B on the lecture :(
            hanoi(3, 'A', 'C', 'B').forEach { logger.info("received $it") }


        }


}