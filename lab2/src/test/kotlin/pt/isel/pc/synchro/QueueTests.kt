package pt.isel.pc.synchro

import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class QueueTests {

    @Test
    fun `a single consumer and producer simple test`() {
        val queue = Queue<Int>()
        var result : Int = 0

        val tconsumer = thread {
            result = queue.take()
        }

        val tproducer = thread {
            Thread.sleep(1000)
            queue.offer(2)
        }

        tproducer.join(3000)
        tconsumer.join(3000)

        assertEquals(2, result)
    }

    @Test
    fun `multiple use with a single consumer and producer test`() {
        val queue = Queue<Int>()
        val consumedValues = mutableSetOf<Int>()
        val NVALUES = 100_000

        val tconsumer = Thread {
            while(true) {
                val res = queue.take()
                if (res < 0) break
                consumedValues.add(res)
            }
        }
        tconsumer.start()

        val tproducer = Thread {
            repeat(NVALUES) {
                queue.offer(it)
            }
            queue.offer(-1)
        }
        tproducer.start()

        tproducer.join(3000)
        tconsumer.join(3000)

        assertEquals(NVALUES, consumedValues.size)
    }

}