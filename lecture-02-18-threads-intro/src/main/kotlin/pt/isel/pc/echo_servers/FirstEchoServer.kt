package pt.isel.pc.echo_servers

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
 * A simple sequential echo server.
 * Since it is single threaded it can accept only one client
 * at a time. Not very useful, indeed!
 *
 * Try this using telnet on Windows.
 * If you have installed a wsl distribution,
 * it's better to use nc (NetCat).
 *
 * The command "ip route" executed inside wsl is a way to
 * find windows host address
 */
class FirstEchoServer(val port: Int, val address: String = "0.0.0.0") {
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
                        writer.writeLine("hello, what do you want to echo?")
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
            serverSock.bind(InetSocketAddress(address, port))
            logger.info("Waiting for client connections")
            while(true) {
                val clientSock = serverSock.accept()
                processConnection(clientSock)
            }
        }
    }
}

private fun main() {
    FirstEchoServer(8000, "0.0.0.0").run()
}

