package pt.isel.pc.echo_client

import mu.KotlinLogging
import java.io.IOException
import pt.isel.pc.echo_servers.utils.writeLine
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}


/**
 * This a class sepcically made to test the solutions
 * for add a client id in the multithreaded echo server
 */
class EchoClient(private val serverAddress: String, private val serverPort : Int) {

    /**
     * this method
     */
    fun contact() : Int {
        var id : Int = 0
        try {
            val socket = Socket()
            socket.use {
                socket.connect(InetSocketAddress(serverAddress, serverPort))
                val reader = socket.getInputStream().bufferedReader()
                val writer = socket.getOutputStream().bufferedWriter()
                reader.use {
                    val line = reader.readLine()
                    val parts = line.split(" ")
                    id  = parts[2].toInt() // get the client id
                }
            }
            // connection closed!
        }
        catch(e: IOException) {
            println("error on connect:${e.message}, ${e.cause?.message}")
        }
        return id
    }


}

fun main() {
    val nclients = 1000

    val time = measureTime {
        val ids = ConcurrentHashMap.newKeySet<Int>()
        val threads = mutableListOf<Thread>()
        repeat(nclients) {
            val thread = Thread {
                val client = EchoClient("127.0.0.1", 8000)
                val res = client.contact()

                if (!ids.add(res)) {
                    println("$res: duplicated id!")
                }

            }

            threads.add(thread)
            thread.start()
            if (it % 20 == 0) TimeUnit.MICROSECONDS.sleep(6000)

        }
        logger.info("wait for threads!")
        for (t in threads) t.join(5000)

        if (nclients != ids.size) {
            println("nclients=$nclients, ids.size=${ids.size}")
        } else {
            println("all ok!")
        }
    }
    println("done in $time ms!")

}