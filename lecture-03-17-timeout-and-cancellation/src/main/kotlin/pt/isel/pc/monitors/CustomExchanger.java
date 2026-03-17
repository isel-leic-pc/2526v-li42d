package pt.isel.pc.monitors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This is a (pseudo) solution to the synchronizer Exchanger
 * produced by Google Gemini via Chrome
 * What can go wrong with this code?
 * @param <T>
 */
public class CustomExchanger<T> {
    private T item;
    private boolean occupied = false;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public T exchange(T x) throws InterruptedException {
        lock.lock();
        try {
            if (!occupied) {
                // Primeiro thread a chegar: guarda o item e espera
                item = x;
                occupied = true;
                while (occupied) {
                    condition.await(); // Aguarda o segundo thread
                }
                // Retorna o item deixado pelo segundo thread
                T response = item;
                item = null; // Limpa para a próxima troca
                condition.signalAll(); // Acorda outros se necessário
                return response;
            } else {
                // Segundo thread a chegar: faz a troca e acorda o primeiro
                T firstItem = item;
                item = x;
                occupied = false;
                condition.signalAll();
                return firstItem;
            }
        } finally {
            lock.unlock();
        }
    }
}