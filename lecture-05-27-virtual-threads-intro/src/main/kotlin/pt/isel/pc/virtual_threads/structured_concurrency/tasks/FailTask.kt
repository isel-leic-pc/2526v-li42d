package virtual_threads3.tasks

class FailTask<T>(name: String) : AbstractTask<T>(name) {
    override fun doWork(): T {
        throw RuntimeException("oops!")
    }
}