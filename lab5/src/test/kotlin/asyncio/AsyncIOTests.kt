package pt.isel.pc.asyncio

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import pt.isel.pc.asyncio.async_sockets.AsyncSocketCF
import pt.isel.pc.asyncio.async_sockets.AsyncSocketCb
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.CountDownLatch

class AsyncIOTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `a raw http request using sockets`() {
        val request = "GET /get HTTP/1.1\r\nHost: \"httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()

        val socket = Socket()

        logger.info("start connection to host")
        val hostAddr = InetSocketAddress("httpbin.org", 80)
        socket.connect(
            hostAddr,
            0
        )
        logger.info("send http request")
        val outputStream = socket.getOutputStream()
        outputStream.write(request)

        val response = ByteArray(1024)
        logger.info("receive http response start")
        val inputStream = socket.getInputStream()
        val nRead = inputStream.read(response)
        logger.info("http response received")
        println(response.decodeToString( 0, nRead, false))

    }

    @Test
    fun `a raw http request using socket operations that invoke callbacks`() {
        val socket = AsyncSocketCb()
        val request = "GET /get HTTP/1.1\r\nHost: \"httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
        val response = ByteArray(1024)
        val cdl = CountDownLatch(1)
        var error : Throwable? = null

        logger.info("start connection to host")
        val hostAddr = InetSocketAddress("httpbin.org", 80)
        socket.connectAsync(hostAddr) { err, res ->
            logger.info("send http request")
            if (err != null) {
                logger.info("error $err on connect")
                error = err
                cdl.countDown()
            } else socket.writeAsync(request) { err, res ->
                if (err != null) {
                    logger.info("error $err on write")
                    error = err
                    cdl.countDown()
                } else {
                    logger.info("receive http response start")
                    socket.readAsync(response, 1024) { err, res ->
                        if (err != null) {
                            logger.info("error $err on read")
                            error = err
                            cdl.countDown()
                        } else {
                            logger.info("http response received: $res bytes")
                            println(response.decodeToString( 0, res!!, false))
                            cdl.countDown()
                        }

                    }
                }
            }
        }

        cdl.await()
        logger.info("done!")
    }

    @Test
    fun `a raw http request using socket operations that return completable futures`() {
        val socket = AsyncSocketCF()
        val request = "GET /get HTTP/1.1\r\nHost: \"httpbin.org\r\nConnection: close\r\n\r\n".encodeToByteArray()
        val response = ByteArray(1024)
        val cdl = CountDownLatch(1)

        logger.info("start connection to host")
        val hostAddr = InetSocketAddress("httpbin.org", 80)
        val res = socket.connectAsync(hostAddr)
        .thenCompose {
            logger.info("send http request")
            socket.writeAsync(request)
        }
        .thenCompose {
            logger.info("receive http response start")
            socket.readAsync(response, 1024)
        }
        .whenComplete { n, throwable ->
            if (throwable != null) {
                logger.info("error: $throwable")
            }
            else {
                logger.info("http response received: $n bytes")
                println(response.decodeToString( 0, n, false))

            }
            cdl.countDown()
        }

        cdl.await()
        logger.info("done!")
    }

    @Test
    fun `a raw http request using socket operations that return completable futures supported by NIO2`() {
        TODO()
    }
}