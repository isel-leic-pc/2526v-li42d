package pt.isel.pc.asyncio.async_sockets

import java.net.Socket
import java.net.SocketAddress

typealias IOContinuation<T> = (e: Throwable?, res: T?) -> Unit

/**
 * A class that wrappers a Java Socket in order to
 * access to it using an asynchronous API, but supported
 * by the creation of threads or a backing threadpool,
 * which is not enough for real asynchronous I/O.
 *
 * The received callback in each operation is called either the operation
 * terminates successfully or with failure
 *
 * Each operation returns without blocking as is the requirement
 * for an asynchronous API
 */
class AsyncSocketCb {
    private val socket = Socket()


    fun connectAsync(endPoint: SocketAddress, cb : IOContinuation<Unit>) {
        Thread {
            try {
                socket.connect(endPoint, 0)
                cb(null, Unit)
            }
            catch(e: Exception) {
                cb(e, null)
            }
        }
        .start()
    }

    fun writeAsync(bytes : ByteArray,len:Int, cb : IOContinuation<Unit>)  {

        Thread {
            try {
                socket.getOutputStream().write(bytes, 0, len)
                cb(null, Unit)
            }
            catch(e: Exception) {
                cb(e, null)
            }
        }
        .start()
    }

    fun writeAsync(bytes : ByteArray, cb : IOContinuation<Unit> )  {
        writeAsync(bytes, bytes.size, cb)
    }

    fun readAsync(bytes : ByteArray,len:Int, cb : IOContinuation<Int> )  {
        Thread {
            try {
                val nRead = socket.getInputStream().read(bytes, 0, len)
                cb(null, nRead)
            }
            catch(e: Exception) {
                cb(e, null)
            }
        }
        .start()
    }


    fun close() {
        socket.close()
    }
}