package logic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

// [snippet:logic-job-tree]
suspend fun work(name: String, durationMs: Long, report: (name: String, progress: Float) -> Unit) {
    val steps = 50
    repeat(steps) { i ->
        delay(durationMs / steps)
        report(name, (i + 1f) / steps)
    }
}

// 回傳每個節點的 Job，讓畫面可以點擊取消
fun CoroutineScope.launchTree(report: (String, Float) -> Unit): Map<String, Job> {
    val jobs = mutableMapOf<String, Job>()
    jobs["Parent"] = launch {
        // 在 Parent 的協程裡 launch → 它們是 Parent 的子協程
        jobs["Child 1"] = launch { work("Child 1", 4000, report) }
        jobs["Child 2"] = launch { work("Child 2", 6000, report) }
        // Parent 自己的程式碼到這裡就跑完了，但它會等兩個子協程結束才算完成
    }
    jobs["Sibling"] = launch { work("Sibling", 5000, report) }
    return jobs
}
// [/snippet]

private val treeNodes = listOf("Scope" to 0, "Parent" to 1, "Child 1" to 2, "Child 2" to 2, "Sibling" to 1)

private fun Job.status(): String = when {
    isCancelled && isCompleted -> "Cancelled"
    isCancelled -> "Cancelling"
    isCompleted -> "Done"
    else -> children.count { it.isActive }.let { if (it > 0) "Waiting for $it" else "Running" }
}

private fun statusColor(status: String) = when (status) {
    "Cancelled", "Cancelling" -> Color(0xFFFF9E9E)
    "Done" -> Color(0xFF9CD67F)
    else -> Color(0xFF80D5D4)
}

@Composable
fun JobTreeDemo() {
    val scope = rememberCoroutineScope()
    val jobs = remember { mutableStateMapOf<String, Job>() }
    val progress = remember { mutableStateMapOf<String, Float>() }
    var tick by remember { mutableIntStateOf(0) }

    // 每 100ms 重新讀一次 Job 的狀態（Job 本身不是 Compose 的 State）
    LaunchedEffect(Unit) {
        while (true) { delay(100); tick++ }
    }

    fun start() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        progress.clear()
        jobs["Scope"] = scope.launch {
            val tree = launchTree { name, p -> progress[name] = p }
            delay(1) // 等 Parent 的程式碼執行完，Child 1 / Child 2 才會出現在 map 裡
            jobs.putAll(tree)
        }
    }

    Column(Modifier.fillMaxSize().background(ConsoleBg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(onClick = ::start) { Text(if (jobs.isEmpty()) "Start" else "Restart") }
        Text("Tap a node to cancel it", color = Color(0xFF8F8A99), fontSize = 12.sp)
        tick // 讀取 tick，讓下面的狀態跟著輪詢更新
        treeNodes.forEach { (name, depth) ->
            val job = jobs[name]
            val status = job?.status() ?: "Not started"
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 18).dp)
                    .background(Color(0xFF2B2930), RoundedCornerShape(8.dp))
                    .clickable(enabled = job?.isActive == true) { job?.cancel() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(name, color = Color(0xFFE6E1E9), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    if (name in listOf("Child 1", "Child 2", "Sibling")) {
                        LinearProgressIndicator(
                            progress = { progress[name] ?: 0f },
                            modifier = Modifier.padding(top = 4.dp).fillMaxWidth().height(4.dp),
                            color = statusColor(status),
                            trackColor = Color(0xFF3B3843),
                        )
                    }
                }
                Text(
                    status,
                    color = statusColor(status),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 10.dp).width(92.dp),
                )
            }
        }
    }
}

// [snippet:logic-cancel]
// 模擬一段吃 CPU 的計算（例如壓縮圖片），每一塊約 150ms，中間不會暫停
private fun crunchChunk() {
    val until = TimeSource.Monotonic.markNow() + 150.milliseconds
    while (until.hasNotPassedNow()) { }
}

suspend fun compress(cooperative: Boolean, log: (String) -> Unit) {
    try {
        for (chunk in 1..8) {
            if (cooperative) yield() // 取消點：被取消就在這裡丟出 CancellationException
            crunchChunk()
            log("[worker] chunk $chunk/8")
        }
        log("[worker] finished all chunks")
    } finally {
        log("[worker] finally: release buffers") // 不論完成或取消都會執行
    }
}

suspend fun compressThenCancel(cooperative: Boolean, log: (String) -> Unit) = coroutineScope {
    val job = launch { compress(cooperative, log) }
    delay(400)
    log("[caller] cancel()")
    job.cancel()
    job.join()
    log("[caller] join() returned, isCancelled=${job.isCancelled}")
}
// [/snippet]

@Composable
fun CancelDemo() = ConsoleDemo(listOf("no check", "yield()")) { option, log ->
    compressThenCancel(cooperative = option == 1, log)
}
