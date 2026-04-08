package pt.isel.pc

import mu.KotlinLogging
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore

private val logger = KotlinLogging.logger {}

/**
 * This an echo server variant that illustrate
 * the use of ThreadScope and shutdown hooks
 *
 * Note that in order to test this code you have to change the
 * void ThreadScope version with your own version.
 */
class EchoServerScoped(val port: Int) {

    companion object {
        val EXIT = "exit"
        val MAX_CLIENTS = 10

    }

    // a semaphore used for sessions limitation
    val sessionsAvailable = Semaphore(MAX_CLIENTS)

    private val serverSocket = ServerSocket()

    // Note the threadScope is constructed with a virtual threads builder
    // in this kind of threads you can interrupt threads blocked in socket operations
    // If you use platform threads socket operations are not interruptible,
    // and as a consequence, scope cancelling will not produce the desired cancellation effect.
    // Using platform threads you have to previous close the sockets
    private val serverScope = ThreadScope("serverScope", 1,Thread.ofVirtual())

    private fun BufferedWriter.writeLine(line: String) {
        appendLine(line)
        flush()
    }

    /**
     * * @param clientSock the socket connected to the client
     */
    private fun processConnection(clientSock: Socket, clientId: Int) {
        logger.info("client ${clientSock.remoteSocketAddress} connected")

        clientSock.use {
            val reader = BufferedReader(InputStreamReader(clientSock.getInputStream()))
            val writer = BufferedWriter(OutputStreamWriter(clientSock.getOutputStream()))
            writer.writeLine("Hello, client $clientId")
            while (true) {
                val line = reader.readLine() ?: break
                if (line == EXIT) {
                    writer.writeLine("Bye, client $clientId")
                    break
                }
                //logger.info("line '$line' received")
                writer.writeLine(line)
            }
        }

    }

    // start a new client in a new child scope
    fun newClient(socket: Socket, id: Int) {
        val childScope = serverScope.newChildScope("child $id", 1);
        childScope?.newThread {
            try {
                processConnection(socket, id)
            }
            catch(e: Throwable) {
                logger.info("run: exception ${e.message} occurred")
            }
            finally {
                sessionsAvailable.release()
            }
        }
    }

    // run the server
    fun run() {
        logger.info("Waiting for connections...")

        serverScope.newThread {
            try {
                serverSocket.bind(InetSocketAddress("0.0.0.0", port))
                serverSocket.use {
                    var clientId = 1
                    while (true) {
                        sessionsAvailable.acquire()
                        val clientSocket = serverSocket.accept()
                        newClient(clientSocket, clientId++)
                    }
                }
            } catch (e: Throwable) {
                logger.info("run: exception ${e.message} occurred")
            }
        }
    }

    // shutdown the server waiting for scopes termination
    fun shutdown() {
        serverScope.cancel()
        serverScope.join()
    }


    // just wait for server termination
    fun join() {
        serverScope.join()
    }
}

fun main() {
    val server =  EchoServerScoped(8000)

    // register shutdown hook illustration
    // hooks run on process termination
    val shutdownThread = Thread {
        logger.info("Starting shutdown process")
        server.shutdown()
        server.join()
    }


    Runtime.getRuntime().addShutdownHook(shutdownThread)

    //   using hooks, you can have trouble to debug server shutdown
    //   an alternative to using hooks for shutdown debugging is just
    //   create a thread that wait for input in order to start the shutdown, as
    //   illustrated below.

    //    val shutdownThread = Thread {
    //        readln()
    //        logger.info("Starting shutdown process")
    //        server.shutdown()
    //        server.join()
    //    }
    //
    //    shutdownThread.start()

    server.run()
    logger.info("Waiting for server termination")
    server.join()
    logger.info("Terminated server")
}