package pt.isel.pc.hazards

import org.junit.jupiter.api.Test


class CaptureTests {

    @Test
    fun capture1() {

        repeat(10) {
            val thread = Thread {
                println(it)
            }
            thread.start()
            thread.join()
        }
    }

    @Test
    fun capture2() {
        val threads = mutableListOf<Thread>()
        repeat(10) {
            val thread = Thread {
                println(it)
            }
            threads.add(thread)
            thread.start()
        }
        threads.forEach { it.join() }
    }

    @Test
    fun capture3() {
        val threads = mutableListOf<Thread>()
        var i= 1
        while(i <= 10) {
            val t = Thread {
                println(i)
            }
            threads.add(t)
            t.start()
            ++i
        }
        threads.forEach { it.join() }
    }
}

