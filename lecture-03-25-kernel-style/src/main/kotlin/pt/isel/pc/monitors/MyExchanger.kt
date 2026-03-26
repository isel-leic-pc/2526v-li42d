package pt.isel.pc.monitors

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * versão que utiliza um unico objecto que representa a troca pendente
 */
class MyExchanger<T> {
    private val mutex = ReentrantLock()
    private val exchangeDone = mutex.newCondition()

    private class PendingExchange<T>(val v1: T, var v2: T? = null)

    // diferente de null representa a troca. A null representa a auseência de troca
    private var pendingExchange  : PendingExchange<T>? = null

    @Throws(InterruptedException::class)
    fun exchange(value: T, timeout: Duration): T? {
         mutex.withLock {
             // fast path
             if (pendingExchange != null) { // troca pendente
                 pendingExchange?.v2 = value
                 exchangeDone.signal()
                 val partnerValue = pendingExchange?.v1
                 pendingExchange = null
                 return partnerValue
             }
             if (timeout == Duration.ZERO) return null

             // wait path

             var timeoutNanos = timeout.inWholeNanoseconds
             val myExchange = PendingExchange(value)
             pendingExchange = myExchange
             try {
                 while (true) {
                     timeoutNanos = exchangeDone.awaitNanos(timeoutNanos)
                     if (myExchange.v2 != null) {
                         return myExchange.v2
                     }
                     if (timeoutNanos <= 0) {
                         pendingExchange = null
                         return null
                     }
                 }
             }
             catch(e: InterruptedException) {
                 // se a operação está resolvida a melhor solução é retornat sucesso
                 // regenerando a interrupção, pois o processo de cancelamento em curso
                 // não se pode perder
                 if (myExchange.v2 != null) {
                     Thread.currentThread().interrupt()
                     return myExchange.v2
                 }
                 pendingExchange = null
                 throw e
             }
         }
    }
}