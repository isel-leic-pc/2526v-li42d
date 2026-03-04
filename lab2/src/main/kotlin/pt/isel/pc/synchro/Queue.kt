package pt.isel.pc.synchro

import java.lang.Thread.sleep
import java.util.LinkedList
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * This class implements a queue
 * where "take" operation block the invoking thread when the list is empty
 * and "offer" operation block the invoking thread when  the list is full.
 * This makes the queue a good solution for producer/consumer scenarios
 * We use two semaphores to achieve this goal
 */
class Queue<T>(capacity: Int = 10){
    private val list = LinkedList<T>()
    private val mutex = ReentrantLock()
    private val availableElems = Semaphore(0)
    private val availableSpace = Semaphore(capacity)

    fun offer(elem: T) {
        availableSpace.acquire()
        mutex.withLock {
            list.add(elem)
        }
        availableElems.release()

    }

    fun take() : T {
        availableElems.acquire()
        val elem = mutex.withLock {
            list.removeFirst()
        }
        availableSpace.release()
        return elem
    }
}