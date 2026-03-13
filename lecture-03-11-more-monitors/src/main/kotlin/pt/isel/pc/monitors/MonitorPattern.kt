package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MonitorPattern<T> {
    private val mutex = ReentrantLock()
    private val condition = mutex.newCondition()

    private fun canProceed1() : Boolean {
        TODO()
    }
    // ...
    private fun canProceedN() : Boolean {
        TODO()
    }

    private fun changeState1() : T {
        TODO()
    }
    // ...
    private fun changeStateN() : T {
        TODO()
    }

    private fun setSignalState1(param: T)   {
        TODO()
    }
    //...
    private fun setSignalStateN(param: T) : T {
        TODO()
    }

    // public interface

    fun monitorFunc1() : T? {
        mutex.withLock {
            while (!canProceed1()) {
                condition.await()
            }
            return changeState1()
        }
    }

    //...
    fun monitorFuncN() : T? {
        mutex.withLock {
            while (!canProceedN()) {
                condition.await()
            }
            return changeStateN()
        }
    }

    fun monitorSignalFunc1(param: T) {
        mutex.withLock {
            setSignalState1(param)
            condition.signal()
        }
    }
    //...
    fun monitorSignalFuncN(param: T) {
        mutex.withLock {
            setSignalStateN(param)
            condition.signal()
        }
    }
}