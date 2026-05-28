package virtual_threads3.tasks

class SuccessTask<T>(name: String, private val value: T) : AbstractTask<T>(name){
    override fun doWork(): T {
        return value
    }
}