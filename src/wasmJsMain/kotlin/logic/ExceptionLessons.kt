package logic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

// [snippet:logic-propagation]
suspend fun loadProduct(useAsync: Boolean, log: (String) -> Unit) {
    try {
        coroutineScope {
            launch {
                try {
                    repeat(6) { delay(200); log("[reviews] loading ${it + 1}/6") }
                } catch (e: CancellationException) {
                    log("[reviews] cancelled")
                    throw e
                }
            }
            if (useAsync) {
                val price = async<Int> { delay(500); error("price API failed") }
                try {
                    price.await()
                } catch (e: IllegalStateException) {
                    log("[product] caught at await(): ${e.message}") // 抓到了，但為時已晚
                }
            } else {
                launch { delay(500); error("price API failed") }
            }
            log("[product] body finished")
        }
    } catch (e: IllegalStateException) {
        log("[caller] coroutineScope threw: ${e.message}")
    }
}
// [/snippet]

@Composable
fun PropagationDemo() = ConsoleDemo(listOf("launch", "async + try/await")) { option, log ->
    loadProduct(useAsync = option == 1, log)
}

// [snippet:logic-supervisor]
suspend fun downloadAll(supervised: Boolean, log: (String) -> Unit) {
    // 只有「沒人接手」的例外才會走到 handler，例如 supervisorScope 底下 launch 失敗
    val handler = CoroutineExceptionHandler { _, e -> log("[handler] ${e.message}") }
    val files = listOf("a.png" to 900L, "b.png" to 500L, "c.png" to 1300L)

    val downloads: suspend CoroutineScope.() -> Unit = {
        for ((file, ms) in files) {
            launch(handler) {
                try {
                    delay(ms)
                    if (file == "b.png") error("$file: HTTP 500")
                    log("[$file] downloaded")
                } catch (e: CancellationException) {
                    log("[$file] cancelled")
                    throw e
                }
            }
        }
    }

    try {
        if (supervised) supervisorScope(downloads) else coroutineScope(downloads)
        log("[caller] scope completed")
    } catch (e: IllegalStateException) {
        log("[caller] caught: ${e.message}")
    }
}
// [/snippet]

@Composable
fun SupervisorDemo() = ConsoleDemo(listOf("coroutineScope", "supervisorScope")) { option, log ->
    downloadAll(supervised = option == 1, log)
}
