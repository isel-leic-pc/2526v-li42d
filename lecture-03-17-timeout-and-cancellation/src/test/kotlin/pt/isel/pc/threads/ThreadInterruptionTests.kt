package pt.isel.pc.threads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

class ThreadInterruptionTests {

    @Test
    fun `interrupt thread in new state`() {
        val t = Thread {}
        assertEquals(Thread.State.NEW, t.state)
        t.interrupt()
        assertEquals(Thread.State.NEW, t.state)
    }

    @Test
    fun `interrupt thread in new state and start after`() {
        var interrupted = false
        var isInterruptedInCatchBlock = false
        val t = Thread {
            try {
                //sleep(2000)
                var sum = 0
                repeat(1_000_000) {
                    //if (Thread.interrupted() ) {
                    if (Thread.currentThread().isInterrupted() ) {
                        throw InterruptedException()
                    }
                    sum += it
                }
                println(sum)
            }
            catch(e: InterruptedException) {
                isInterruptedInCatchBlock = Thread.currentThread().isInterrupted
                interrupted = true
            }
        }
        assertEquals(Thread.State.NEW, t.state)
        t.interrupt()
        t.start()
        assertEquals(Thread.State.RUNNABLE, t.state)
        t.join()
        assertEquals(Thread.State.TERMINATED, t.state)
        assertTrue(interrupted)
        println(isInterruptedInCatchBlock)
    }

    @Test
    fun `interrupt thread blocked on a sleep`() {
        TODO()
    }
}