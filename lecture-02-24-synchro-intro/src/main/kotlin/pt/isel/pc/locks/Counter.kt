package pt.isel.pc.locks

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.atomic.AtomicLong
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
        mutex.withLock {
            value--
        }
    }

    fun get() : Long {
        // to modify
        mutex.withLock {
            return value
        }

    }
}

class Counter2( value: Long = 0) {
    private val avalue = AtomicLong(value)
    private val mutex = ReentrantLock()

    fun inc() {
        avalue.incrementAndGet()
    }

    fun dec() {
        avalue.decrementAndGet()

    }

    fun get() : Long {
       return avalue.get()
    }
}