package pt.isel.pc.locks

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Counter(private var value: Long = 0) {
    private val mutex = ReentrantLock()
    fun inc() {
//        mutex.lock()
//        try {
//            value++
//        }
//        finally {
//            mutex.unlock()
//        }

        mutex.withLock {
            value++
        }

    }

    fun dec() {
        // to modify
        value--
    }

    fun get() : Long {
        // to modify
        return value
    }
}