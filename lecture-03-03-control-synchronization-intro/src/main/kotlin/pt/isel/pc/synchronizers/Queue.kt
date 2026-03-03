package pt.isel.pc.synchronizers

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
class Queue<T>(capacity: Int = 4){
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

    /**
     * The following "badTake" functions are all "bad" in the sense that
     * we should not (amd will not) implement take with "busy waiting", since
     * the waiting time is completely indeterminate, could be micros, could be minutes!
     * They are just more or less "bad", illustrating common bad synchronization scenarios.
     */

    /**
     * First try for busy waiting a non-empty list.
     * This is very wrong since the busy wait is done
     * inside the lock, possibly leading to a deadlock
     * (since offer threads can't acquire the lock)
     */
    fun badTake0() : T {
        mutex.withLock {
            while (list.isEmpty()) { }
            return list.removeFirst()
        }
    }

    // Observation: in general the above code will produce a compilation error.
    // The following is more common:
    //        return mutex.withLock {
    //            while(list.isEmpty()) {}
    //            list.removeFirst()
    //        }

    // But it's ok, since the withLock function is marked as "inline", which means that is injected directly
    // in the function badTake0, as it was written like this:
    //
    //        mutex.lock()
    //        try {
    //            while(list.isEmpty()) {}
    //            return list.removeFirst()
    //        }
    //        finally {
    //            mutex.unlock()
    //        }

    /**
     * Second try for busy waiting a non-empty list.
     * Here the problem is that we have a classic Non-Atomic Check And Act.
     * In other words, the list can be observed as NonEmpty, but before acquiring the lock
     * in line 69 another "take" could have happened, the list is now "empty", and removeFirst throws an exceptiom
     *
     */
    fun badTake1() : T {
        while(true) {
            mutex.withLock {
                if (list.isNotEmpty())
                    break
            }
        }
        mutex.withLock {
            return list.removeFirst();
        }
    }

    /**
     * In this version we solve the previous problem
     * checking again inside the final lock if the
     * list is not empty and retry all if it is empty!
     */
    fun badTake2() : T {
        while(true) {
            while (true) {
                mutex.withLock {
                    if ((list.isNotEmpty()))
                        break
                }
                // try to give the processor to another thread
                // in order to avoid monopolize the processor
                sleep(0)

            };
            mutex.withLock {
                if (list.isNotEmpty())
                    return list.removeFirst()
            }
        }
    }

    // after the "BUSY waiting" pseudo solutions, we have
    // the correct solution using the auxiliary semaphores
    fun take() : T {
        availableElems.acquire()
        val elem = mutex.withLock {
            list.removeFirst()
        }
        availableSpace.release()
        return elem
    }
}