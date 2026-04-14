package pt.isel.pc.mm


fun main() {
    while(true) {
        print("s? ")
        val s = readln().toDouble()
        val n = 200000
        val speedUp: Double = 1 / (s + (1 - s) / n)
        println("speedup = $speedUp")
    }
}