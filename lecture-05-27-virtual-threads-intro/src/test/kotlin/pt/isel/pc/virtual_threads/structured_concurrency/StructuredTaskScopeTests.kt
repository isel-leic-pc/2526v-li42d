package pt.isel.pc.virtual_threads.structured_concurrency

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import virtual_threads3.tasks.Task
import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.Subtask
import java.util.concurrent.StructuredTaskScope.Joiner
import java.util.stream.Stream
import kotlin.use


class StructuredTaskScopeTests {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    private fun <T, R> StructuredTaskScope<T, R>.fork1(block: () -> T): Subtask<T> {
        return this.fork(Callable { block() })
    }

    @Test
    fun `a simple scope with three successful subtasks of the same result type`() {
        logger.info("start test")
        StructuredTaskScope.open<String>().use { scope ->
            val startTime = System.currentTimeMillis()

            val task1 = scope.fork1 { Task.success<String?>("t1", "Ok1").exec(500)  }
            val task2 = scope.fork1 { Task.success<String?>("t2", "Ok2").exec(1000) }
            val task3 = scope.fork1 { Task.success<String?>("t3", "Ok3").exec(800)  }

            val res = scope.join()

            if (res is Stream<*>) {
                res.forEach {
                    println((it as Subtask<String>).get())
                }
            }

            println("Done in ${System.currentTimeMillis() - startTime} ms!")
            println("task 1 result = " + task1.get())
            println("task 2 result = " + task2.get())
            println("task 3 result = " + task3.get())
        }
        logger.info("end test")
    }

    @Test
    fun `a simple scope with three subtasks where one or more fail`() {
        logger.info("start test")
        try {
            StructuredTaskScope.open<Any,Any >(Joiner.anySuccessfulResultOrThrow()).use { scope ->
                val startTime = System.currentTimeMillis();
                var task1 = scope.fork1 { Task.fail<Any>("t1").exec(600) }
                var task2 = scope.fork1 { Task.fail<Any>("t2").exec(300) }
                var task3 = scope.fork1 { Task.success("t3", "Ok3").exec(3000) }

                scope.join();

                println("Done in ${System.currentTimeMillis() - startTime} ms!")

            }
        } catch (e: Exception) {
            logger.info("catch error $e")
        }
        logger.info("end test")
    }


    private fun inner(name: String): String =
        StructuredTaskScope.open<String, Any>(Joiner.anySuccessfulResultOrThrow()).use { scope ->
            var task1 = scope.fork1 { Task.success(name + "-1", "child-res-1").exec(3000) }
            var task2 = scope.fork1 { Task.fail<String>(name + "-2").exec(200) }

            scope.join()

            "${task1.get()} " //+
                  //  "and ${task2.get()}"
        }

    @Test
    @Throws(InterruptedException::class)
    fun `a composed scope with two tasks  and one subtask that fail`() {
        try {
            StructuredTaskScope.open<Any>().use { scope ->
                val task1 = scope.fork1 { Task.longRun("t1").exec(6000) }
                val task2 = scope.fork1 { Task.success<String?>("t2", "Ok2").exec(5000) }
                val task3 = scope.fork1 { inner("t3") }
                val res = scope.join()
//
//                println("task 2 result = " + task2.get())
//                println("task 3 result = " + task3.get())
            }
        } catch (e: java.lang.Exception) {
            println("error $e on scope: ")
        }
    }

    private fun inner2( name: String) : String =
        try {
            StructuredTaskScope.open<String>().use { scope ->
                var task1 = scope.fork1 { Task.success(name + "-1", "child-res-1").exec(3000) }
                var task2 = scope.fork1 { Task.success(name + "-1", "child-res-1").exec(3000) }
                scope.join();
                "${task1.get()} and ${task2.get()}"
            }
        }
        catch(e: InterruptedException) {
            println("Interrupt exception ocurrs in inner2");
            throw e
        }



    @Throws(InterruptedException::class)
    private fun inner1(name: String?): String? {
        try {
            StructuredTaskScope.open<Any?>().use { scope ->
                val task1 = scope.fork<String> { Task.success<String?>(name + "-1", "child-res-1").exec(3000) }
                val task2 = scope.fork<String> { inner2(name + "-2") }
                scope.join()
                return String.format("%s and %s", task1.get(), task2.get())
            }
        } catch (e: InterruptedException) {
            println("Interrupt exception ocurrs in inner1")
            throw e
        }
    }



    @Test
    @Throws(InterruptedException::class)
    fun aComposedScopeWithTwoTasksAndTwoSubTasksRootScopeFail() {
        logger.info("start test")
        try {
            StructuredTaskScope.open<Any?>().use { scope ->
                val task1 = scope.fork<Any?>    { Task.longRun("t1").exec(2000) }
                val task2 = scope.fork<Any?>    { Task.fail<Any?>("t2").exec(500) }
                val task3 = scope.fork<String?> { inner2("t3") }
                scope.join()

                println("task 1 result = " + task1.get())
                println("task 2 result = " + task2.get())
                println("task 3 result = " + task3.get())
            }
        } catch (e: java.lang.Exception) {
            println("on root scope, catch error $e")
        }
        logger.info("end test")
    }
}