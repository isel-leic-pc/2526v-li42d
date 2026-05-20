package pt.isel.pc

interface KStream<T> {
    suspend fun tryAdvance(cons: suspend (T) -> Unit) : Boolean

    companion object {
        fun  <T> from(list: List<T>) : KStream<T> {
            return object : KStream<T> {
                val srcIt = list.iterator()
                override suspend fun tryAdvance(cons: suspend (T) -> Unit): Boolean {
                    return if (!srcIt.hasNext()) false
                    else {
                        cons(srcIt.next())
                        true
                    }
                }
            }
        }

        fun <T> iterate( seed : T, oper: (T) -> T) : KStream<T> {
            return object : KStream<T> {
                var curr = seed
                override suspend fun tryAdvance(cons: suspend (T) -> Unit): Boolean {
                    cons(curr)
                    curr = oper(curr)
                    return true
                }
            }
        }
    }
}

fun <T,U> KStream<T>.map( mapper : suspend (T) -> U) : KStream<U> {
    return object : KStream<U> {
        override suspend fun tryAdvance(cons : suspend (U) -> Unit) : Boolean {
            return this@map.tryAdvance {
                cons(mapper(it))
            }
        }
    }
}

fun <T> KStream<T>.take( limit: Int) : KStream<T> {
    return object : KStream<T> {
        var remaining = limit
        override suspend fun tryAdvance( cons :suspend (T) -> Unit) : Boolean {
            if (remaining <= 0) return false
            --remaining
            return this@take.tryAdvance {
                cons(it)
            }
        }
    }
}

suspend fun <T> KStream<T>.forEach(action : suspend (T)-> Unit)  {
    while (tryAdvance(action)  ) {}
}