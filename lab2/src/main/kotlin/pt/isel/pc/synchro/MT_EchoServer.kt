package pt.isel.pc.synchro

import mu.KotlinLogging
import pt.isel.pc.utils.writeLine
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * A multithreaded echo server.
 * Since each client is served in a new threaded,
 * it can accept multiple clients at a time.
 * But this is not a good solution, in general,
 * since it is not scalable.
 * Each thread consumes significant resources
 * The result is the server limit is reached with a few thousand
 * simultaneous clients
 *
 * Try this using telnet on Windows.
 * If you have installed a wsl distribution,
 * it's better to use nc (NetCat).
 *
 * The command ip route executed inside wsl is a way to
 * find windows host address
 */
class MT_EchoServer(val port: Int, val address: String = "0.0.0.0") {
    companion object {
        val EXIT = "bye"
        val logger = KotlinLogging.logger {}
    }

    private fun processConnection(clientSock: Socket) {
        logger.info("client ${clientSock.remoteSocketAddress} connected")
        clientSock.use {
            val reader = BufferedReader(InputStreamReader(clientSock.getInputStream()))
            val writer = BufferedWriter(OutputStreamWriter(clientSock.getOutputStream()))
            reader.use {
                writer.use {
                    try {
                        writer.writeLine("hello, client ")
                        while(true) {
                            val line = reader.readLine() ?: break
                            if (line == EXIT) {
                                writer.writeLine("bye")
                                break;
                            }
                            logger.info("line '$line' received")

                            // echo the line
                            writer.writeLine(line)

                        }

                    }
                    catch(exc: IOException) {
                        logger.info("socket access error")
                    }
                }
            }

        }
        logger.info("client ${clientSock.remoteSocketAddress} disconnected")
    }

    fun run() {
        ServerSocket().use { serverSock ->
            var clientId = 0
            serverSock.bind(InetSocketAddress(address, port))
            logger.info("Waiting for client connections")
            while(true) {
                val clientSock = serverSock.accept()
                clientId++
                // launch a new java platform thread
                // for processing of the new client.
                // As we will see later, this is not a very scalable solution!
                var thread = Thread {
                    processConnection(clientSock)
                }
                thread.start()
            }
        }
    }
}

private fun main() {
    MT_EchoServer(8000, "0.0.0.0").run()
}
