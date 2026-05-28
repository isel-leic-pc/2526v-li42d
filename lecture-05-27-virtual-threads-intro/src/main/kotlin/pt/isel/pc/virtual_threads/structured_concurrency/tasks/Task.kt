package virtual_threads3.tasks


interface Task<T> {
    companion object {
        fun longRun(name: String): Task<Any> {
            return LongRunTask(name)
        }

        fun <T> fail(name: String): Task<T> {
            return FailTask<T>(name)
        }

        fun <T> success(name: String, value: T): Task<T> {
            return SuccessTask(name, value)
        }
    }

    @Throws(RuntimeException::class)
    fun exec(time: Long): T
    fun name(): String
    fun time(): Long

}