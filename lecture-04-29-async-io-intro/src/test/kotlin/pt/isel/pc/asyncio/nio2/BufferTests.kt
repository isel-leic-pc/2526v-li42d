package pt.isel.pc.asyncio.nio2

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer


class BufferTests {
    companion object {
        private val logger = KotlinLogging.logger {}
        private fun logBB(msg: String, bb: ByteBuffer) {
            logger.info(
                "$msg - position: ${bb.position()}, limit: ${bb.limit()}, capacity: ${ bb.capacity()}"
            )
        }
    }


    @Test
    fun `simple buffer use example`() {
        // Create
        val bb = ByteBuffer.allocate(16)
        logBB("after allocate", bb)

        // Write
        bb.put(1)
        logBB("after put 1", bb)
        bb.put(2)
        logBB("after put 2", bb)

        // Read
        bb.flip()
        logBB("after flip", bb)
        var b = bb.get()
        logBB("after get $b", bb)
        b = bb.get()
        logBB("after get $b", bb)

        b = bb.get()
        logBB("after get $b", bb)
        bb.clear()
        logBB("after clear", bb)
    }
}