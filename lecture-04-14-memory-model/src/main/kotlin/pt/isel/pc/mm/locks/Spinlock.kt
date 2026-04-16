package pt.isel.pc.mm.locks

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Bad implementation!
 * Why?
 */
class Spinlock0 {
    val locked =  AtomicBoolean()

    fun lock( ) {
        while(true) {
            while(locked.get()) {
                Thread.onSpinWait()
            }
            locked.set(true)
            break
        }
    }

    fun unlock( ) {
        locked.set(false)
    }
}

/**
 * Ok
 * Use of compareAndSet avoid the race of the previous implementation
 */
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