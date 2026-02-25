package pt.isel.pc.hazards

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis


/**
 * multiple (some ok, some bad) variants of concurrent access to
 * a hashmap, to update (increment) key values
 */
class HashMapsHazardTests {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val N_THREADS = 8
        private val N_KEYS = 5_000_000
    }

    /**
     * auxiliary function to do in parallel some action (updater)
     */
    private fun parallelMapFiller(updater : (Int) -> Unit) : Long {
        return measureTimeMillis {
            val threads = (0..<N_THREADS)
                .map {
                    Thread {
                        for (i in 1..N_KEYS) {
                            updater(i)
                        }
                    }
                }
            threads.forEach { thread ->
                thread.start()
            }
            threads.forEach { thread ->
                    thread.join()
            }
        }
    }

    /**
     * auxiliary function to return the sum of the hashMap int values
     */
    private fun hashSumValues(hashMap: MutableMap<Int, out Any>) : Int {
        return hashMap.values
            .map { value ->
                when(value) {
                    is Int -> value
                    is AtomicInteger -> value.get()
                    else -> 0
                }

            }
            .reduce{ a , e -> a+e }
    }

    // In this Test we use a basic HashMap
    // the map as an Int key (between 1 and N_KEYS)
    // and the associated values count he number of accesses made to each key
    // Since the map is filled with 5_000_000 keys using 8 threads to fill each key in parallel,
    // the sum of all key values (40_000_000)
    // Is this map thread safe?
    @Test
    fun countWithBasicMapTest() {
        val basic = HashMap<Int, AtomicInteger>()

        val millis = parallelMapFiller { key ->
            val value = basic[key] ?: AtomicInteger()
            value.incrementAndGet()
            basic[key] = value
        }
        logger.info ("basic hashMap fill done in $millis ms!")
        assertEquals(N_THREADS * N_KEYS, hashSumValues(basic))
    }

    // this is a test for the same operations of the previous test
    // but using functional api of the basic HashMap
    // Is now the code thread safe?
    @Test
    fun countWithBasicMapWithComputeIfAbsentTest() {
        val basic = HashMap<Int, AtomicInteger>()

        val millis = parallelMapFiller { key ->
            basic.computeIfAbsent(key) { value -> AtomicInteger() }
            .incrementAndGet()
        }
        logger.info ("basic hashMap fill done in $millis ms!")
        assertEquals(N_THREADS * N_KEYS, hashSumValues(basic))
    }

    // this is a test for the same operations of the previous test
    // but using now a synchronized hashmap, that is a wrapper the use a lock
    // to provide atomic access to each map operation
    // Is the synchronized map thread safe?
    @Test
    fun countWithSynchronizedMapTest() {
        val synchMap: MutableMap<Int, AtomicInteger> =
            Collections.synchronizedMap(HashMap())

        val millis = parallelMapFiller { key->
            val value = synchMap[key] ?: AtomicInteger()
            value.incrementAndGet()
            synchMap[key] = value
        }
        logger.info("synchronized hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(synchMap))
    }

    // this is a test for the same operations of the previous test
    // but using now a dedicated lock in test to provide atomic access
    // Is the resulting code thread safe?
    @Test
    fun countWithOurSynchroMapTest() {
        val ourSynchroMap = HashMap<Int, Int>()
        val mutex = ReentrantLock()

        val millis = parallelMapFiller { key ->
            mutex.withLock {
                val value = ourSynchroMap[key] ?: 0
                ourSynchroMap[key] = value + 1
            }
        }

        logger.info("our synchro hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(ourSynchroMap))
    }

    // this is a test for the same operations of the previous test
    // but using now a ConcurrentHashMap to provide concurrent access to de map
    // Is the resulting code thread safe?
    @Test
    fun countWithConcurrentMapTest() {
        val concurrentMap: MutableMap<Int, AtomicInteger> = ConcurrentHashMap()

        val millis = parallelMapFiller { key ->
            concurrentMap
                .computeIfAbsent(key) { value -> AtomicInteger() }
                .incrementAndGet()
        }
        logger.info("concurrent hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(concurrentMap))
    }

}