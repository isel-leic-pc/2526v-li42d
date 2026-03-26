package pt.isel.pc.monitors

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Nesta versão usa-se a forma tradicional de implementação
 * do kernel style usando uma lista de operações pendentes.
 * É fácil de perceber que a lógica de funcionamente do sincronizador garante que
 * a lista só pode conter no máximo um elemento, que representa a (única) troca pendente.
 * A próxima operação de exchange resolve a troca colocando a lista de novo vazia.
 * A versão MyExchange evita a lista usando uma referência nullable.
 */
class MyExchanger0<T> {
    private val mutex = ReentrantLock()
    private val exchangeDone = mutex.newCondition()


    /**
     * Classe que representa uma operação de exchange pendente
     * Cada instância é criada pela thread que observa ausência de troca pendente
     * A próxima thread resolve a troca esvaziando de nova a lista
     */
    private class PendingExchange<T>(val v1: T, var v2: T? = null)

    private var pendingExchanges  = LinkedList<PendingExchange<T>>()

    @Throws(InterruptedException::class)
    fun exchange(value: T, timeout: Duration): T? {
        mutex.withLock {
            // fast path
            if (pendingExchanges.isEmpty()) {
                val pe = pendingExchanges.removeFirst()
                pe.v2 = value
                exchangeDone.signal()
                val partnerValue = pe.v1
                return partnerValue
            }
            if (timeout == Duration.ZERO) return null

            // wait path

            var timeoutNanos = timeout.inWholeNanoseconds
            val myExchange = PendingExchange(value)
            pendingExchanges.addLast(myExchange)
            try {
                while (true) {
                    timeoutNanos = exchangeDone.awaitNanos(timeoutNanos)
                    if (myExchange.v2 != null) {
                        return myExchange.v2
                    }
                    if (timeoutNanos <= 0) {
                        pendingExchanges.remove(myExchange)
                        return null
                    }
                }
            }
            catch(e: InterruptedException) {
                if (myExchange.v2 != null) {
                    Thread.currentThread().interrupt()
                    return myExchange.v2
                }
                pendingExchanges.remove(myExchange)
                throw e
            }
        }
    }
}