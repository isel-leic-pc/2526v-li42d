package pt.isel.pc.asyncio.async_sockets

import pt.isel.pc.asyncio.nio2.connectAsyncCF
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.CompletableFuture

/**
 * A partial implementation of an asynchronous socket API
 * using CompletableFutures that is backed by a NIO2 AsynchronousSocketChannel
 *
 * Using the bridge developed in "nio_bridges.kt"
 */
class AsyncSocketChannel {
    val channel = AsynchronousSocketChannel.open()

    fun connectAsync(endPoint: InetSocketAddress) : CompletableFuture<Void> {
        return  channel.connectAsyncCF(endPoint)
    }

}