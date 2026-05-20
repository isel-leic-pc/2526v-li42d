package pt.isel.pc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import kotlin.test.Test

class EagerVersusLazyTests {

    private suspend fun inc(value: Int) : Int {
        delay(1000)
        return value + 1
    }

    fun incNormal(value: Int) : Int {
        sleep(1000)
        return value + 1
    }

    @Test
    fun `check eagerness of List`() {
        runBlocking {
            val list = listOf(1, 2, 3, 4, 5)
                .map {
                    println("on list, inc $it")
                    inc(it)
                }
                .take(3)
            println("start list forEach")
            list.forEach {
                println(it)
            }
        }
    }

    @Test
    fun `check laziness of KStream`() {
        val kStream = KStream.iterate(1) { it + 1 }
            .map {
                println("on kStream, inc $it")
                inc(it)
            }
            .take(3)


        runBlocking {
            println("start kStream collect")
            kStream.forEach {
                println(it)
            }
        }
    }

    @Test
    fun `check laziness of Sequence`() {
        val seq = sequence<Int> {
            repeat(10) {
                yield(it)
            }
        }
        .map {
            println("on sequence map, incNormal $it")
            // impossible to compile with inc suspend
            incNormal(it)
        }
        .take(3)


        println("start sequence forEach")
        seq.forEach {
            println(it)
        }

    }
}