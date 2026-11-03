package logic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

// [snippet:logic-suspend]
// suspend 版：delay 期間把執行緒讓出來，其他協程可以繼續跑
suspend fun prepare(name: String, log: (String) -> Unit) {
    repeat(3) { step ->
        log("[$name] step ${step + 1}")
        delay(300)
    }
}

// 阻塞版：忙等 300ms，相當於 Thread.sleep(300)，這段期間執行緒被霸佔
fun prepareBlocking(name: String, log: (String) -> Unit) {
    repeat(3) { step ->
        log("[$name] step ${step + 1}")
        val until = TimeSource.Monotonic.markNow() + 300.milliseconds
        while (until.hasNotPassedNow()) { /* 什麼都不做，只是佔著執行緒 */ }
    }
}

// 兩個協程在「同一條執行緒」上執行
suspend fun prepareTwo(blocking: Boolean, log: (String) -> Unit) = coroutineScope {
    launch { if (blocking) prepareBlocking("A", log) else prepare("A", log) }
    launch { if (blocking) prepareBlocking("B", log) else prepare("B", log) }
}
// [/snippet]

@Composable
fun SuspendDemo() = ConsoleDemo(listOf("suspend: delay()", "blocking: busy wait")) { option, log ->
    prepareTwo(blocking = option == 1, log)
}

// [snippet:logic-builders]
private suspend fun fetch(what: String, ms: Long, log: (String) -> Unit): String {
    log("[$what] request")
    delay(ms)
    log("[$what] response")
    return what
}

suspend fun loadDashboard(log: (String) -> Unit): String = coroutineScope {
    // async：需要結果的工作，回傳 Deferred<T>，之後用 await() 取值
    val user = async { fetch("user", 600, log) }
    val feed = async { fetch("feed", 900, log) }
    // launch：不需要結果的工作，回傳 Job
    launch {
        delay(1200)
        log("[analytics] page view sent")
    }
    val screen = "${user.await()} + ${feed.await()}"
    log("[dashboard] ready: $screen")
    screen
} // coroutineScope 會等「所有」子協程結束才返回，包括 analytics

suspend fun openDashboard(log: (String) -> Unit) {
    log("[caller] loadDashboard()")
    val screen = loadDashboard(log)
    log("[caller] returned \"$screen\"")
}
// [/snippet]

@Composable
fun BuildersDemo() = ConsoleDemo { _, log -> openDashboard(log) }
