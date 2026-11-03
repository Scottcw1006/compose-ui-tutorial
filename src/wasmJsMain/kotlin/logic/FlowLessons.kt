package logic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlin.random.Random

// [snippet:logic-backpressure]
enum class Strategy { Default, Buffer, Conflate, CollectLatest }

// 感測器每 100ms 送出一筆
fun sensorReadings(log: (String) -> Unit): Flow<Int> = flow {
    for (i in 1..6) {
        delay(100)
        log("[sensor] emit $i")
        emit(i)
    }
}

// 但畫面處理一筆要 300ms
suspend fun render(value: Int, log: (String) -> Unit) {
    log("[ui] start $value")
    delay(300)
    log("[ui] rendered $value")
}

suspend fun consume(strategy: Strategy, log: (String) -> Unit) {
    val readings = sensorReadings(log)
    when (strategy) {
        Strategy.Default -> readings.collect { render(it, log) }
        Strategy.Buffer -> readings.buffer().collect { render(it, log) }
        Strategy.Conflate -> readings.conflate().collect { render(it, log) }
        Strategy.CollectLatest -> readings.collectLatest { render(it, log) }
    }
}
// [/snippet]

@Composable
fun BackpressureDemo() = ConsoleDemo(listOf("collect", "buffer()", "conflate()", "collectLatest")) { option, log ->
    consume(Strategy.entries[option], log)
}

// [snippet:logic-combine]
fun ticks(name: String, periodMs: Long, count: Int): Flow<String> = flow {
    for (i in 1..count) {
        delay(periodMs)
        emit("$name$i")
    }
}

suspend fun mergeQuotes(useZip: Boolean, log: (String) -> Unit) {
    val prices = ticks("p", 300, 5).onEach { log("[prices] $it") }
    val rates = ticks("r", 700, 2).onEach { log("[rates] $it") }
    val quotes =
        if (useZip) prices.zip(rates) { p, r -> "$p × $r" } // 一對一配對，等慢的那邊
        else combine(prices, rates) { p, r -> "$p × $r" }  // 任一邊更新，就用雙方「最新值」重算
    quotes.collect { log("[quote] $it") }
}
// [/snippet]

@Composable
fun CombineDemo() = ConsoleDemo(listOf("combine", "zip")) { option, log ->
    mergeQuotes(useZip = option == 1, log)
}

// [snippet:logic-hot-flow]
class PriceRepository(scope: CoroutineScope, log: (String) -> Unit) {
    // 冷流：沒有人收集就不會執行；每個收集者各自執行一份
    private val livePrices: Flow<Int> = flow {
        log("[upstream] connect")
        try {
            var price = 100
            while (true) {
                delay(500)
                price += Random.nextInt(-3, 4)
                emit(price)
            }
        } finally {
            log("[upstream] disconnect")
        }
    }

    // 轉成熱流：所有收集者共用同一條上游；最後一個收集者離開 2 秒後才真正斷線
    val price: StateFlow<Int> = livePrices.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 2000),
        initialValue = 100,
    )
}
// [/snippet]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotFlowDemo() {
    val scope = rememberCoroutineScope()
    val log = remember { TimelineLog() }
    val repo = remember { PriceRepository(scope, log::log) }
    val subscribers = remember { mutableStateListOf<Pair<Int, Job>>() }
    val latest = remember { mutableStateMapOf<Int, Int>() }
    val nextId = remember { intArrayOf(1) }

    Column(Modifier.fillMaxSize().background(ConsoleBg)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val id = nextId[0]++
                    log.log("[screen $id] start collecting")
                    subscribers += id to scope.launch { repo.price.collect { latest[id] = it } }
                }) { Text("+ Collector") }
                OutlinedButton(
                    enabled = subscribers.isNotEmpty(),
                    onClick = {
                        val (id, job) = subscribers.removeAt(subscribers.lastIndex)
                        job.cancel()
                        latest.remove(id)
                        log.log("[screen $id] stop collecting")
                    },
                ) { Text("− Collector") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (subscribers.isEmpty()) Text("No collectors", color = Color(0xFF8F8A99), fontSize = 12.sp)
                subscribers.forEach { (id, _) ->
                    Text(
                        "screen $id: ${latest[id] ?: "…"}",
                        color = Color(0xFFE6E1E9),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color(0xFF2B2930), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
        LogView(log, Modifier.weight(1f))
    }
}
