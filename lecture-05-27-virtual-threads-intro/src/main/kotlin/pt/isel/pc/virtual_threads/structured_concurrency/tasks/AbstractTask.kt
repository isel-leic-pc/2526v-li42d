package virtual_threads3.tasks

import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory


abstract class AbstractTask<T>(name: String) : Task<T> {
    companion object {
        val logger = KotlinLogging.logger {}

        fun setThreadName() {
            val name = Thread.currentThread().toString()
            val type = if (name.startsWith("Virtual") ) 'v' else 'p'
            val start = name.indexOf('#')+1
            val end = name.indexOf(']')
            val number = name.substring(start,end).toInt()
            Thread.currentThread().name = "$type/$number"
        }
    }

    private val name: String
    private var time: Long = 0

    init {
        this.name = name
    }

    override fun name(): String {
        return name
    }

    override fun time(): Long {
        return time
    }

    @Throws(RuntimeException::class)
    override fun exec(time: Long): T {
        setThreadName()
        try {
            this.time = time
            if (time > 0) Thread.sleep(time)
            val result: T = doWork()
            logger.info("Task {} Completed!", name())
            return result
        } catch (e: InterruptedException) {
            logger.info("Task {} Cancelled!", name())
            throw e
        } catch (e: RuntimeException) {
            logger.info("Task {} Failed!", name())
            throw e
        }
    }

    @Throws(InterruptedException::class)
    protected abstract fun doWork(): T
}