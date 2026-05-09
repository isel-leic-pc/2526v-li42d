package pt.isel.pc.asyncio.async_sockets

import pt.isel.pc.asyncio.nio2.connectAsync
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.CompletableFuture

/**
 * A partial implementation of an asynchronous socket API
 * using CompletableFutures that is backed by a NIO2 AsynchronousSocketChannel
 *
 * Using the bridge developed in "nio_bridges.kt"
 *
 * Taking advantage of the bridge operations for completable futures
 * from NIO2 operations, complete the class below with asynchronous operations
 * for connect, read and write, returning completable futures
 */
class AsyncSocketChannel {
    private val channel = AsynchronousSocketChannel.open()


    // TO COMPLETE
}