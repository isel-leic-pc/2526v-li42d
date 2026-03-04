package pt.isel.pc.synchro

/**
 * A (very) simple ThreadPool
 * using the implemented Queue
 * thread pools are normally better alternatives
 * to explicit thread creation, since they avoid the creation/destruction "cost"
 * and limits the number of used threads
 */
class SimpleThreadPool private constructor() {

    companion object {
        // factory method to pool creation
        // that avoid
        fun create() : SimpleThreadPool {
            val pool = SimpleThreadPool()
            pool.start()
            return pool
        }
    }
    private val requests = Queue<Runnable>()

    private val threads = mutableListOf<Thread>()

    init {
        repeat(Runtime.getRuntime().availableProcessors()) {
            threads.add( Thread {
                workerFun()
            })
        }
    }

    // the code executed by the worker threads
    private fun workerFun() {
        while(true) {
            val work = requests.take()
            work.run()
        }
    }

    private fun start() {
        threads.forEach {
                thread -> thread.start()
        }
    }

    /**
     * Submit work to the pool
     * in the form of a Runnable
     */
    fun submit(work: Runnable) {
        requests.offer(work)
    }

}