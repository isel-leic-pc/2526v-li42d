package pt.isel.pc.locks

class PettersonLock0 {
    private val interested =  Array(2) { false}

    private fun other (index: Int) : Int =
        if (index == 0) return 1; else return 0

    fun lock(index : Int) {
        interested[index] = true
        while(interested[other(index)]) {}
    }

    fun unlock(index: Int) {
        interested[index] = false
    }
}