package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext


fun CoroutineContext.getInfo() : String {
    val builder = StringBuilder("context elements:\n")
    this.fold(builder) {
            builder, ctx->
        val str = "\t${ctx.key.toString()}"
        builder.append("${str.substring(0, str.indexOf('$'))} : $ctx\n")
        builder
    }
    builder.append("end\n")
    return builder.toString()
}

private fun Job.internalState(builder: StringBuilder, level: Int)  : String
{
    indent(builder, level)
    builder.append('[')
    if (isCancelled && isCompleted) builder.append("Cancelled")
    else if (isCancelled) builder.append("Cancelling")
    else if (isCompleted) builder.append("Completed or Completing")
    else if (isActive) builder.append("Active")
    builder.append(']')
    return builder.toString()

}

fun Job.state() : String =
    internalState( StringBuilder(), 0)

fun indent(builder: StringBuilder, level : Int) {
    repeat(level*4) {
        builder.append(' ')
    }
}

private fun Job.internalGetInfo(builder : StringBuilder, level: Int) : String {
    indent(builder,level)
    builder.append("job $this\n")
    if (children.count() > 0) {
        indent(builder,level+1)
        builder.append("childs:\n")
        for (childJob in children) {
            builder.append("${childJob.internalGetInfo(StringBuilder(), level + 2)}")
        }
        indent(builder,level+1)
        builder.append("end\n")
    }

    indent(builder,level+1)
    builder.append("parent: $parent\n")
    indent(builder,level+1)
    builder.append("state: ${state()}\n")
    return builder.toString()
}


fun Job.getInfo() : String =
    internalGetInfo(StringBuilder().apply { append('\n')},0)
