package pt.isel.pc.asyncio.async_sockets

import java.net.Socket
import java.net.SocketAddress
import java.util.concurrent.CompletableFuture

/**
 * A class that wrappers a Java Socket in order to
 * access to it using an asynchronous API, but supported
 * by the creation of threads or a backing threadpool,
 * which is not enough for real asynchronous I/O.
 *
 * Each operation return a CompletableFuture that represents it's execution
 * being it successfully or with failure
 *
 * Each operation returns without blocking as is the requirement
 * for an asynchronous API
 */
class AsyncSocketCF {
    private val socket = Socket()

    fun connectAsync(endPoint: SocketAddress) : CompletableFuture<Unit> {

        val cf = CompletableFuture<Unit>()

        Thread {
            try {
                socket.connect(endPoint, 0)
                cf.complete(Unit)
            }
            catch(e: Exception) {
                cf.completeExceptionally(e)
            }
        }
        .start()
        return cf
    }

    fun writeAsync(bytes : ByteArray,len:Int ) : CompletableFuture<Unit> {
        //         val cf = CompletableFuture<Unit>()
        //        Thread {
        //            try {
        //                socket.getOutputStream().write(bytes, 0, len)
        //                cf.complete(Unit)
        //            }
        //            catch(e: Exception) {
        //                cf.completeExceptionally(e)
        //            }
        //        }
        //        .start()
        //        return cf
        //
        // The code below is a better alternative since it used the
        // CompletableFuture associated threadpool, but is not yet real asynchronouis I/O
        return CompletableFuture.supplyAsync {
            socket.getOutputStream().write(bytes, 0, len)
        }
    }

    fun writeAsync(bytes : ByteArray ) : CompletableFuture<Unit> {
        return writeAsync(bytes, bytes.size)
    }

    fun readAsync(bytes : ByteArray,len:Int ) : CompletableFuture<Int> {
        val cf = CompletableFuture<Int>()

        Thread {
            try {
                val nRead = socket.getInputStream().read(bytes, 0, len)
                cf.complete(nRead)
            }
            catch(e: Exception) {
                cf.completeExceptionally(e)
            }
        }
        .start()
        return cf
    }
}