// To access the *non-public* Continuation API
// ONLY for learning purposes
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package pt.isel.pc.virtual_threads.sequences

import jdk.internal.vm.Continuation
import jdk.internal.vm.ContinuationScope

class Sequence<T>(val block : Sequence<T>.SequenceIterator.() -> Unit) : Iterable<T> {

    private val scope = ContinuationScope("SequenceScope")

    inner class SequenceIterator : Iterator<T> {
        private var nextValue : T? = null

        private var cont = Continuation(scope) {
            block.invoke(this)
        }

        override fun hasNext(): Boolean {
            if (nextValue != null) return true
            cont.run()
            return !cont.isDone && nextValue != null
        }

        override fun next(): T {
            if (!hasNext()) throw IllegalStateException()
            val value = nextValue
            nextValue = null
            return value!!
        }

        fun yield(t: T) {
            nextValue = t
            Continuation.yield(scope)
        }
    }

    override fun iterator(): Iterator<T> {
        return SequenceIterator()
    }

    // generators

    companion object {
         fun <T> iterate(initial: T, func: ( (T) -> T))  =
             Sequence {
                 var next = initial
                 while(true) {
                     val n = next
                     next = func(next)
                     yield(n)
                 }
             }


        fun range(min: Int, max: Int): Sequence<Int> =
            Sequence {
                var curr = min
                while (curr <= max) {
                    yield(curr)
                    curr += 1
                }
            }
    }


    fun  limit(limit: Int) : Sequence<T> {
        val src = this
        return Sequence<T>()  {
            var curr = 0;
            val itSrc = src.iterator()
            while (curr++ < limit && itSrc.hasNext()) {
                yield(itSrc.next());
            }
        }
    }

    fun  filter(pred: ( (T) -> Boolean) ) : Sequence<T> {
        val src = this
        return Sequence<T>()  {
            val itSrc = src.iterator()

            while(itSrc.hasNext()) {
                var res: T? = null
                while (res == null && itSrc.hasNext()) {
                    val r = itSrc.next()
                    if (pred(r)) {
                        res = r;
                    }
                }
                if (res != null) yield(res);
            }
        }
    }

    fun <U,V> zip(other : Sequence<U>, combiner: ((t: T, u: U) -> V) ): Sequence<V> {
        var src  = this
        return Sequence {
            val itOther = other.iterator()

            val itSrc = src.iterator()
            while(itSrc.hasNext() && itOther.hasNext()) {
                yield(combiner(itSrc.next(), itOther.next()));
            }
        }
    }
}