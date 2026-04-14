package pt.isel.pc.mm.locks

import java.util.concurrent.atomic.AtomicIntegerArray

class PettersonLock1 {
    private val interested =  AtomicIntegerArray(2)

    @Volatile
    private var turn = 0

    private fun other (index: Int) : Int =
        if (index == 0) return 1; else return 0

    fun lock(index : Int) {
        interested[index] = 1
        turn = other(index)
        while( turn == other(index) && interested[other(index)] != 0) {}
    }

    fun unlock(index: Int) {
        interested[index] = 0
    }
}