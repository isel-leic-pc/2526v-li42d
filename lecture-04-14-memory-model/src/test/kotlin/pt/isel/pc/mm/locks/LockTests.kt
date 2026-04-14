package pt.isel.pc.mm.locks

import pt.isel.pc.locks.PettersonLock
import pt.isel.pc.locks.PettersonLock0
import kotlin.test.Test
import kotlin.test.assertEquals

class LockTests {

    @Test
    fun `check counting using pettersonlock test`() {
        val mutex = PettersonLock()
        var counter = 0

        val t1 = Thread {
            repeat(10000) {
                mutex.lock(0)
                counter++
                mutex.unlock(0)
            }
        }

        val t2 = Thread {
            repeat(10000) {
                mutex.lock(1)
                counter++
                mutex.unlock(1)
            }
        }

        t1.start()
        t2.start()

        t1.join()
        t2.join()

        assertEquals(20000, counter)
    }

    @Test
    fun `check counting using spinlock test`() {
        val mutex = Spinlock()
        var counter = 0

        val t1 = Thread {
            repeat(10000) {
                mutex.lock()
                counter++
                mutex.unlock()
            }
        }

        val t2 = Thread {
            repeat(10000) {
                mutex.lock()
                counter++
                mutex.unlock()
            }
        }

        t1.start()
        t2.start()

        t1.join()
        t2.join()

        assertEquals(20000, counter)
    }


    @Test
    fun `check counting using pettersonlock0 with atomic array test`() {
        val mutex = PettersonLock0()
        var counter = 0

        val t1 = Thread {
            repeat(10000) {
                mutex.lock(0)
                counter++
                mutex.unlock(0)
            }
        }

        val t2 = Thread {
            repeat(10000) {
                mutex.lock(1)
                counter++
                mutex.unlock(1)
            }
        }

        t1.start()
        t2.start()

        t1.join()
        t2.join()

        assertEquals(20000, counter)
    }
}