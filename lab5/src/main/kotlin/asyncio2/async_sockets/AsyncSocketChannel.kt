package pt.isel.pc.asyncio2.async_sockets

import java.nio.channels.AsynchronousSocketChannel

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