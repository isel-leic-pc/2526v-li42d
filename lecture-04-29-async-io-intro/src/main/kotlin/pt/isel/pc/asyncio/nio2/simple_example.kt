package pt.isel.pc.asyncio.nio2

import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val socket = AsynchronousSocketChannel.open()
    val latch = CountDownLatch(1)
    logger.info("connecting...")
    socket.connect(
        InetSocketAddress("httpbin.org", 80),
        Unit,
        object : CompletionHandler<Void, Unit> {
            override fun completed(result: Void?, attachment: Unit, ) {
                logger.info("connected")
                latch.countDown()
            }

            override fun failed( exc: Throwable, attachment: Unit, ) {
                logger.info("connect failed - {}", exc.message)
                latch.countDown()
            }
        },
    )
    logger.info("after connect call")
    latch.await()
    logger.info("done")
}


