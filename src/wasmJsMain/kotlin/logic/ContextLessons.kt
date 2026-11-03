package logic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

// [snippet:logic-context]
private fun CoroutineContext.describe() =
    "name=${this[CoroutineName]?.name}, on Default=${this[ContinuationInterceptor] == Dispatchers.Default}"

suspend fun syncPhotos(log: (String) -> Unit) {
    log("[caller] ${coroutineContext.describe()}")

    // withContext：換掉部分元素（這裡是 dispatcher 與 name），執行完再換回來
    withContext(Dispatchers.Default + CoroutineName("sync")) {
        log("[sync] ${coroutineContext.describe()}")
        val syncJob = coroutineContext[Job]!!

        launch {
            // 子協程繼承 name 與 dispatcher，但會拿到一個新的 Job，掛在 syncJob 底下
            log("[child] ${coroutineContext.describe()}, child of sync=${coroutineContext[Job] in syncJob.children}")
        }
        launch(CoroutineName("upload")) {
            // 傳進 launch 的元素會覆蓋繼承來的同類元素
            log("[upload] ${coroutineContext.describe()}")
        }
    }

    log("[caller] ${coroutineContext.describe()}")
}
// [/snippet]

@Composable
fun ContextDemo() = ConsoleDemo { _, log -> syncPhotos(log) }
