package pt.isel.pc.asyncio.nio2

import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

typealias IOContinuation<T> = (Throwable?, T) -> Unit


private val logger = KotlinLogging.logger {}

val IO_ERROR = -2
val EOF = -1

// Bridge to functional continuations

private val acceptCompleted = object : CompletionHandler<AsynchronousSocketChannel?,
        IOContinuation<AsynchronousSocketChannel?>> {
    override fun completed(result: AsynchronousSocketChannel?,
                           attach: IOContinuation<AsynchronousSocketChannel?>) {
        attach(null, result)
    }

    override fun failed(exc: Throwable, attach: IOContinuation<AsynchronousSocketChannel?>) {
        attach(exc, null)
    }
}

private val rwCompleted = object  : CompletionHandler<Int, IOContinuation<Int>> {
    override fun completed(result: Int, attach: IOContinuation<Int>) {
        attach(null, result)
    }

    override fun failed(exc: Throwable, attach: IOContinuation<Int>) {
        attach(exc, IO_ERROR)
    }
}

fun AsynchronousServerSocketChannel.accept(cont: IOContinuation<AsynchronousSocketChannel?>) {
    this.accept(cont, acceptCompleted )
}

fun AsynchronousSocketChannel.read (dst: ByteBuffer, cont: IOContinuation<Int>) {
    this.read(dst, cont, rwCompleted)
}


fun AsynchronousSocketChannel.write (src: ByteBuffer, cont: IOContinuation<Int>) {
    this.write(src, cont, rwCompleted)
}

// bridges to completable futures

private val connectCompletedCF = object  : CompletionHandler<Void, CompletableFuture<Void>> {
    override fun completed(result: Void, attach: CompletableFuture<Void>) {
        attach.complete(result)
    }

    override fun failed(exc: Throwable, attach: CompletableFuture<Void>) {
        attach.completeExceptionally(exc)
    }
}

private val acceptCompletedCF = object : CompletionHandler<AsynchronousSocketChannel,
        CompletableFuture<AsynchronousSocketChannel>> {
    override fun completed(result: AsynchronousSocketChannel,
                           attach: CompletableFuture<AsynchronousSocketChannel>) {
         attach.complete(result)
    }

    override fun failed(exc: Throwable, attach:  CompletableFuture<AsynchronousSocketChannel>) {
        attach.completeExceptionally(exc)
    }
}

private val rwCompletedCF = object  : CompletionHandler<Int, CompletableFuture<Int>> {
    override fun completed(result: Int, attach: CompletableFuture<Int>) {
        attach.complete(result)
    }

    override fun failed(exc: Throwable, attach: CompletableFuture<Int>) {
        attach.completeExceptionally(exc)
    }
}

fun AsynchronousServerSocketChannel.acceptAsyncCF() : CompletableFuture<AsynchronousSocketChannel> {
    val cf = CompletableFuture<AsynchronousSocketChannel>()
    accept(cf, acceptCompletedCF )
    return cf
}

fun AsynchronousSocketChannel.connectAsyncCF(endPoint: InetSocketAddress) : CompletableFuture<Void> {
    val cf = CompletableFuture<Void>()
    this.connect(endPoint, cf, connectCompletedCF )
    return cf
}


fun AsynchronousSocketChannel.read (dst: ByteBuffer ) : CompletableFuture<Int>{
    val cf = CompletableFuture<Int>()
    this.read(dst, cf, rwCompletedCF)
    return cf
}


fun AsynchronousSocketChannel.writeAsync (src: ByteBuffer) : CompletableFuture<Int> {
    val cf = CompletableFuture<Int>()
    this.write(src, cf, rwCompletedCF)
    return cf
}

// bridges to coroutines

private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()



suspend fun AsynchronousServerSocketChannel.acceptSuspend()
        : AsynchronousSocketChannel {
    TODO()
}

suspend fun AsynchronousSocketChannel.readSuspend (
    dst: ByteBuffer) : Int {
   TODO()
}

suspend fun AsynchronousSocketChannel.writeSuspend (
    dst: ByteBuffer) : Int {
    TODO()
}

private val CR = 13.toByte()
private val LF = 10.toByte()


suspend fun AsynchronousSocketChannel.readLine(): String? {
    val buffer = ByteBuffer.allocate(4096)
    val n =  readSuspend(buffer)

    if (n <= 0) return  null
    var pos = buffer.position()
    if (buffer.get(pos-1) == LF)
        buffer.position(--pos)
    if (pos > 0 && buffer.get(pos-1)  == CR)
        buffer.position(--pos)
    buffer.flip()
    return decoder.decode(buffer).toString()
}

private fun putBuffer(text: String, buffer: ByteBuffer) {
    buffer.clear()
    buffer.put(charSet.encode(text))
    buffer.put(CR)
    buffer.put(LF)
    buffer.flip()
}

suspend fun AsynchronousSocketChannel.writeLine(str: String)  {
    val buffer = ByteBuffer.allocate(4096)
    putBuffer(str, buffer)
    writeSuspend(buffer)
}
