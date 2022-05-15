package cbdc.corda.flow.util

import kotlin.system.measureTimeMillis

inline fun <R> measureTime(block: () -> R): Pair<R, Long> {
    var result: R? = null
    val elapsedTime = measureTimeMillis {
        result = block()
    }
    return result!! to elapsedTime
}

fun <R> Pair<R, Long>.evalTime(block: (Long) -> Unit): R {
    block(second)
    return first
}
