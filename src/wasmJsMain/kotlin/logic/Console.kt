package logic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// 邏輯層範例共用的「執行紀錄」面板。
// 片段本身是純 Kotlin，只透過 log("[name] ...") 回報發生了什麼；
// 時間戳、依 [name] 上色、Run / Stop 按鈕都在這裡處理，不屬於教學內容。

// 範例在這個 dispatcher 上執行：每次恢復協程都排成一個獨立的瀏覽器任務（setTimeout 0），
// 所以 yield() 會真的把執行緒讓給計時器與其他協程，和 Android 主執行緒的行為一致。
// （Compose 預設的 dispatcher 會一口氣跑完排隊中的協程，計時器插不進來。）
object MacrotaskDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) = scheduleMacrotask { block.run() }
}

private fun scheduleMacrotask(block: () -> Unit): Unit = js("setTimeout(block, 0)")

class LogLine(val ms: Long, val tag: String?, val text: String, val system: Boolean)

class TimelineLog {
    val lines = mutableStateListOf<LogLine>()
    private val tagOrder = mutableMapOf<String, Int>()
    private var start: TimeMark? = null // 從第一筆紀錄開始計時

    fun clear() {
        lines.clear()
        tagOrder.clear()
        start = null
    }

    fun log(text: String) = add(text, system = false)
    fun system(text: String) = add(text, system = true)

    fun colorOf(tag: String): Color = LaneColors[tagOrder.getOrPut(tag) { tagOrder.size } % LaneColors.size]

    private fun add(text: String, system: Boolean) {
        val match = TagPattern.find(text)
        val tag = match?.groupValues?.get(1)
        if (tag != null) colorOf(tag)
        val mark = start ?: TimeSource.Monotonic.markNow().also { start = it }
        lines += LogLine(mark.elapsedNow().inWholeMilliseconds, tag, match?.let { text.substring(it.range.last + 1) } ?: text, system)
    }
}

private val TagPattern = Regex("""^\[([^\]]+)]\s*""")

val ConsoleBg = Color(0xFF1E1D24)
private val TimeColor = Color(0xFF6F6A7A)
private val TextColor = Color(0xFFE6E1E9)
private val LaneColors = listOf(
    Color(0xFF80D5D4), Color(0xFFF6C177), Color(0xFFC3A6FF), Color(0xFFFF9E9E), Color(0xFF9CD67F), Color(0xFF8AB4F8),
)

@Composable
fun LogView(log: TimelineLog, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.lines.size) {
        if (log.lines.isNotEmpty()) listState.scrollToItem(log.lines.lastIndex)
    }
    LazyColumn(
        modifier.fillMaxWidth().background(ConsoleBg).padding(horizontal = 12.dp, vertical = 8.dp),
        state = listState,
    ) {
        items(log.lines) { line ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = TimeColor)) { append("+${line.ms}ms ".padStart(9)) }
                    if (line.tag != null) withStyle(SpanStyle(color = log.colorOf(line.tag))) { append("[${line.tag}] ") }
                    withStyle(SpanStyle(color = if (line.system) TimeColor else TextColor, fontStyle = if (line.system) FontStyle.Italic else FontStyle.Normal)) {
                        append(line.text)
                    }
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

// 選一個情境 → Run → 看紀錄。run 收到的是使用者選的選項索引與 log 函式。
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConsoleDemo(
    options: List<String> = emptyList(),
    run: suspend (option: Int, log: (String) -> Unit) -> Unit,
) {
    val log = remember { TimelineLog() }
    val scope = rememberCoroutineScope()
    var option by remember { mutableIntStateOf(0) }
    var job by remember { mutableStateOf<Job?>(null) }
    var running by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(ConsoleBg)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (options.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEachIndexed { i, label ->
                        FilterChip(
                            selected = option == i,
                            onClick = { option = i },
                            enabled = !running,
                            label = { Text(label, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (running) {
                    OutlinedButton(onClick = { job?.cancel() }) { Text("Stop") }
                } else {
                    Button(onClick = {
                        log.clear()
                        running = true
                        val choice = option
                        job = scope.launch(MacrotaskDispatcher) {
                            try {
                                run(choice, log::log)
                                log.system("— finished")
                            } catch (e: CancellationException) {
                                log.system("— stopped")
                                throw e
                            } catch (e: Throwable) {
                                log.system("— uncaught ${e::class.simpleName}: ${e.message}")
                            } finally {
                                running = false
                            }
                        }
                    }) { Text("Run") }
                }
                if (log.lines.isEmpty() && !running) Text("Press Run to start", color = TimeColor, fontSize = 13.sp)
            }
        }
        LogView(log, Modifier.weight(1f))
    }
}
