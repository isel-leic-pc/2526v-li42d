package pt.isel.pc.mm.locks

import java.util.concurrent.atomic.AtomicBoolean

class Spinlock {
    val locked =  AtomicBoolean()

    fun lock( ) {
       while(true) {
           while(locked.get()) {
              Thread.onSpinWait()
           }
           if (locked.compareAndSet(false,true)) break
       }
    }

    fun unlock( ) {
        locked.set(false)
    }
}