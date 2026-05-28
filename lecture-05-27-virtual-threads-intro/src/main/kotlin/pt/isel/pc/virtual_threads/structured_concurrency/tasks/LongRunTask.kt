package virtual_threads3.tasks

class LongRunTask(name: String) : AbstractTask<Any>(name){

    @Throws(InterruptedException::class)
    override fun doWork(): Any {
        var sum: Long = 0
        val startTime = System.currentTimeMillis()
        for (i in 0..<Int.Companion.MAX_VALUE) {
            for (j in 0..3) {
                if (Thread.interrupted()) {
                    throw InterruptedException()
                }
                if ((System.currentTimeMillis() - startTime) >= time()) return Any()
                sum += j
            }
        }
        return Any()
    }
}