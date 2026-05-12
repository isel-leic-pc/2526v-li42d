package pt.isel.pc.coroutines.utils

import kotlinx.coroutines.Job
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

private val corInfoLock = ReentrantLock()

fun CoroutineContext.getInfo() : String {
    return corInfoLock.withLock {
        val builder = StringBuilder("context elements:\n")
        this.fold(builder) {
                builder, ctx->
            val str = "\t${ctx.key.toString()}"
            var last = str.indexOf('$')
            if (last == -1) last = str.length
            builder.append("${str.substring(0, last)} : $ctx\n")
            builder
        }
        builder.append("end\n")
        builder.toString()
    }
}

val Job.state : String
    get() {
        corInfoLock.withLock {
            val builder = StringBuilder("[")
            if (isCancelled) builder.append(" Cancelled")
            if (isCompleted) builder.append(" Completed")
            if (isActive)    builder.append(" Active")
            builder.append(" ]")
            return builder.toString()
        }
    }

suspend fun Job.getInfo() : String {
    return corInfoLock.withLock {
        val builder = StringBuilder()
        builder.append("job $this childs:\n")
        for (childJob in children) {
            builder.append("\t$childJob\n")
        }
        builder.append("end\n")

        builder.append("parent: $parent\n")
        builder.append("state: $state\n")
        builder.toString()
    }
}