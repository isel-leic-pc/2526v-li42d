package pt.isel.pc.coroutines.basics

import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}
private val cdl = CountDownLatch(3)

private fun f1() {
    f2();
    sleep(1000)
    logger.info("1...")
}

private fun f2() {
    f3();
    sleep(1000)
    logger.info("2...")
}

private fun f3() {
    sleep(1000)
    logger.info("3...")
}

class MyContinuation (private val cont : () -> Unit ) {
    fun resume() {
        thread(isDaemon= true){
            cont()
        }
    }
}

private fun f1(previous: MyContinuation ) {
    logger.info("f1 enter")
    val cont = MyContinuation {
        sleep(1000)
        logger.info("1...")
        cdl.countDown()
        previous.resume()
    }
    f2(cont)
    logger.info("f1 leave")
}

private fun f2(previous: MyContinuation) {
    logger.info("f2 enter")
    val cont = MyContinuation {
        sleep(1000)
        logger.info("2...")
        cdl.countDown()
        previous.resume()
    }
    f3(cont)
    logger.info("f2 leave")
}


private fun f3(previous: MyContinuation) {
    logger.info("f3 enter")
    sleep(1000)
    logger.info("3...")
    previous.resume()
    logger.info("f3 leave")
}

private fun continuationChain() {
    sleep(1000);
    logger.info("Start...")
    var cont = MyContinuation {
        sleep(1000)
        logger.info("Go!")
        cdl.countDown()
    }
    f1(cont)
    logger.info("After call f1")
    cdl.await()
    logger.info("End...")
}

private fun functionChain() {
    sleep(1000);
    logger.info("Start...")
    f1();
    sleep(1000)
    logger.info("Go!")
}

private fun main() {
    continuationChain();

}
