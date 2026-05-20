package pt.isel.pc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import pt.isel.pc.utils.getInfo


class ContextPreservationTests {
    
    @Test
    fun `flow emit must always be in collectors context`() {
        runBlocking {
           
            val numsFlow = flow {

                println("first flow repeat: ${coroutineContext.getInfo()}")
                repeat(2) {
                    delay(100)
                    emit(it)
                }

                withContext(Dispatchers.Default) {
                    println("second flow repeat: ${coroutineContext.getInfo()}")
                    repeat(2) {
                        delay(100)
                        emit(it+2)
                    }
                }
            }

            numsFlow.collect {
                println(it)
            }

        }
    }
}