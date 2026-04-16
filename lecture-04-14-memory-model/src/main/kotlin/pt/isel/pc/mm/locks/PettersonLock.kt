package pt.isel.pc.locks

class PettersonLock {


    private val interested =  Array(2) { false}
    @Volatile
    private var turn = 0

    private fun other (index: Int) : Int =
        if (index == 0) return 1; else return 0

    fun lock(index : Int) {
        interested[index] = true
        turn = other(index)
        while( turn == other(index) && interested[other(index)] ) {}
    }

    fun unlock(index: Int) {
        interested[index] = false
    }
}