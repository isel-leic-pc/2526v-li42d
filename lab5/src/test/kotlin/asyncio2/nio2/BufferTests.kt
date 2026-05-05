package pt.isel.pc.asyncio.nio2

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.charset.Charset


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

    @Test
    fun `a_light_less_simple_buffer use example`() {
        // Create
        val charSet = Charset.forName("UTF8")
        val decoder = charSet.newDecoder()

        val bb = ByteBuffer.allocate(32)
        logBB("after allocate", bb)

        val text = "produção"
        bb.put(charSet.encode(text))
        logBB("after put 1", bb)

        // Write
        bb.put(46)
        logBB("after put 2", bb)


        val byteArray = ByteArray(10) {
                index -> (index+65).toByte()
        }
        bb.put(byteArray)
        logBB("after put 2", bb)

        // Read
        bb.flip()
        logBB("after flip", bb)
        val  charBuf = decoder.decode(bb)
        logBB("after decode", bb)

        // clear
        bb.clear()
        logBB("after clear", bb)

        logger.info("charBuf content='$charBuf'")
    }
}