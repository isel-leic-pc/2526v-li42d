package pt.isel.pc.virtual_threads.echo_server

import mu.KotlinLogging
import utils.writeLine
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * A multithreaded echo server using a virtual thread per connection
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
                            logger.info("line '$line' received on thread ${Thread.currentThread()}")

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
                // launch a new java virtual thread
                // for processing of the new client.
                // This is not a very scalable solution!
                var thread = Thread.ofVirtual().start {
                    processConnection(clientSock)
                }
            }
        }
    }
}

private fun main() {
    MT_EchoServer(8000, "0.0.0.0").run()
}
