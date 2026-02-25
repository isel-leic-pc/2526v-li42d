package pt.isel.pc.utils

import kotlin.system.measureTimeMillis

/**
 * An auxiliary function to generate strings used in search algorithms
 */
fun buildString(prefix: String, suffix : Char, total_size: Int) : String {

    val sb = StringBuilder()
    sb.append(prefix)
    repeat(total_size - prefix.length) {
        sb.append(suffix)
    }
    return sb.toString()
}

/**
 * An auxiliary that replicates the strings received in a large array
 * to produce a suitable test to search_parallel algorithm
 */
fun stringsBuilder(total: Int, templates : List<String>) : Array<String> {

    return  IntRange(0, total / templates.size-1).flatMap  {
                templates
            }.toTypedArray()

}

/**
 * Executes a given searching function multiple times and measures its performance
 * in terms of execution time, then prints the result with the minimum time taken
 * and the total matching elements found.
 *
 * @param T the type of elements in the input array
 * @param function a function that performs a search in the array and returns the count
 *        of elements satisfying a predicate. The provided function should accept an array
 *        of type T and a predicate function as parameters.
 * @param values the array of elements to be processed
 * @param prefix a string used as a prefix in the output message
 * @param pred a predicate function used to determine whether an element satisfies a condition
 */
fun <T> test( function : (Array<T>, (T) -> Boolean) -> Int,
              values: Array<T>, prefix: String, pred: (T) -> Boolean ) {
    var minTime = Long.MAX_VALUE
    var total = 0
    repeat(5) {
        var curTotal : Int
        val time = measureTimeMillis{
            curTotal =  function(values, pred)
        }
        if (time < minTime) {
            minTime = time
            total = curTotal
        }
    }

    println("$prefix found $total in ${minTime} ms!")
}