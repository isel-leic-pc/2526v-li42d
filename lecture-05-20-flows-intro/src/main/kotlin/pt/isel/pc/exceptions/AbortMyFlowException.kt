package pt.isel.pc.exceptions

import kotlin.coroutines.cancellation.CancellationException

class AbortMyFlowException(
    val owner: Any
) : CancellationException()