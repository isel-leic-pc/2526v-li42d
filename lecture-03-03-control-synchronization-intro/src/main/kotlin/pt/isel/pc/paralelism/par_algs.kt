package pt.isel.pc.paralelism

import com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time
import pt.isel.pc.utils.buildString
import pt.isel.pc.utils.stringsMaker
import pt.isel.pc.utils.stringsMaker
import pt.isel.pc.utils.test
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Count (in serial) the times the "pred" predicate returns true for each value
 * of array "values"
 */
fun <T> search(values: Array<T> , pred : (T) -> Boolean) : Int {
    var count = 0
    for(i in 0 until values.size) {
        if (pred(values[i])) {
            count++
        }
    }
    return count
}


/**
 * Count (in parallel) the times the "pred" predicate returns true for each value
 * of array "values"
 */
fun <T> search_parallel(values: Array<T> , pred : (T) -> Boolean) : Int {
    TODO()
}

private fun main() {
    val prefix = buildString( prefix ="", suffix = '0', total_size = 10_000)
    val str1 = buildString( prefix, '1', total_size = 20_000)
    val str2 = buildString( prefix, '2', total_size = 20_000)

    val templates = listOf (str1, str2)
    val values = stringsMaker(2_000_000, templates)
    val ref = values[0]
    println(values[0])
    println(values[1])

    test(::search, values, "sequential") {  str ->
        str == ref
    }

    test(::search_parallel, values, "parallel") {  str ->
        str == ref
    }
}