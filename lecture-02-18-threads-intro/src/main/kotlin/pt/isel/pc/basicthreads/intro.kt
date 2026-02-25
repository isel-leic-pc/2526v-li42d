package pt.isel.pc.basicthreads

import mu.KotlinLogging
import java.lang.Thread.sleep

val logger = KotlinLogging.logger {}

fun main() {
    logger.info("test started on thread ${Thread.currentThread()}")

    val thread = Thread {
        sleep(2000)
        logger.info("message from new thread")
    }
    // we have just created a thread object. Note the state os the thread below
    logger.info("thread state before start: ${thread.state}")
    thread.setDaemon(true)
    thread.start()
    sleep(100)
    logger.info("thread state after start: ${thread.state}")
    // change sleep to, say 500,or comment the next line, and see the different states of the created thread
    sleep(300)
    logger.info("thread state before join: ${thread.state}")
    // the method "join" provides a synchronization point
    // The caller will remains blocked until the called thread terminates.
    // but if the thread is already terminated the join will return immediately
    //thread.join()

    logger.info("thread state after join: ${thread.state}")
    logger.info("test terminated on thread thread ${Thread.currentThread()}")
}