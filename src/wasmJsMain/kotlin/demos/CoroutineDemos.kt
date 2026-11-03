package demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.TimeSource

// [snippet:co-launched-effect]
private val fruits = listOf(
    "Apple", "Apricot", "Avocado", "Banana", "Blueberry", "Cherry", "Grape",
    "Kiwi", "Lemon", "Mango", "Melon", "Orange", "Peach", "Pear", "Plum",
)

@Composable
fun DebouncedSearchDemo() {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(fruits) }
    var searching by remember { mutableStateOf(false) }
    var requests by remember { mutableIntStateOf(0) }

    // key 是 query：query 一改變，上一個協程會被取消，再用新的 query 重新啟動
    LaunchedEffect(query) {
        searching = true
        delay(500) // 使用者 500ms 內又打字，協程就在這裡被取消，不會送出請求
        requests++
        results = fruits.filter { it.contains(query, ignoreCase = true) }
        searching = false
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search fruit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            if (searching) "Waiting for you to stop typing..." else "Requests sent: $requests",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(results) { Text(it, modifier = Modifier.padding(vertical = 4.dp)) }
        }
    }
}
// [/snippet]

// [snippet:co-scope]
@Composable
fun ScopeLaunchDemo() {
    // 綁定這個 Composable 的生命週期：離開畫面時，裡面啟動的協程會一起被取消
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var status by remember { mutableStateOf("Idle") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Text(status, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !running,
                onClick = {
                    // 先在點擊當下更新狀態：launch 是排程執行，等協程開始才設 running 的話，快速連點會啟動兩次
                    running = true
                    progress = 0f
                    status = "Downloading..."
                    // onClick 不是 suspend 函式，要透過 scope.launch 進入協程
                    job = scope.launch {
                        try {
                            for (i in 1..100) {
                                delay(40) // delay 是取消點：cancel() 之後會在這裡丟出 CancellationException
                                progress = i / 100f
                            }
                            status = "Done"
                        } catch (e: CancellationException) {
                            status = "Cancelled at ${(progress * 100).roundToInt()}%"
                            throw e // 重新丟出：不要把取消當成一般錯誤吞掉、讓後面的程式繼續執行
                        } finally {
                            running = false
                        }
                    }
                },
            ) { Text("Start") }
            OutlinedButton(enabled = running, onClick = { job?.cancel() }) { Text("Cancel") }
        }
    }
}
// [/snippet]

// [snippet:co-parallel]
// 模擬兩個彼此獨立的網路請求
private suspend fun fetchUser(): String {
    delay(1000)
    return "Ada"
}

private suspend fun fetchPostCount(): Int {
    delay(1500)
    return 42
}

@Composable
fun ParallelDemo() {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("Pick a strategy") }

    fun load(parallel: Boolean) {
        loading = true // 在 launch 之前設定，避免快速連點啟動兩次
        scope.launch {
            val start = TimeSource.Monotonic.markNow()
            val text = if (parallel) {
                // async 立刻啟動、回傳 Deferred；await 才等待結果 → 兩個請求同時進行
                val user = async { fetchUser() }
                val posts = async { fetchPostCount() }
                "${user.await()} · ${posts.await()} posts"
            } else {
                // 一般的 suspend 呼叫會依序執行：等第一個完成才開始第二個
                "${fetchUser()} · ${fetchPostCount()} posts"
            }
            result = "$text\n${start.elapsedNow().inWholeMilliseconds} ms"
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text(result, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !loading, onClick = { load(parallel = false) }) { Text("Sequential") }
            Button(enabled = !loading, onClick = { load(parallel = true) }) { Text("Parallel") }
        }
    }
}
// [/snippet]

// [snippet:co-produce-state]
sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val name: String, val followers: Int) : ProfileState
    data class Error(val message: String) : ProfileState
}

private suspend fun loadProfile(attempt: Int): ProfileState.Success {
    delay(1200)
    if (attempt == 1) error("Network timeout") // 第一次故意失敗，示範錯誤狀態
    return ProfileState.Success("Ada Lovelace", 1815 + attempt)
}

@Composable
fun ProduceStateDemo() {
    var attempt by remember { mutableIntStateOf(1) }

    // 把一段 suspend 程式轉成 State；key（attempt）改變時會取消並重新執行
    val state by produceState<ProfileState>(initialValue = ProfileState.Loading, attempt) {
        value = ProfileState.Loading
        value = try {
            loadProfile(attempt)
        } catch (e: CancellationException) {
            throw e // 取消不是錯誤，不能被當成 Error 吞掉
        } catch (e: Exception) {
            ProfileState.Error(e.message ?: "Unknown error")
        }
    }

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        when (val s = state) {
            ProfileState.Loading -> CircularProgressIndicator()
            is ProfileState.Success -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(s.name, style = MaterialTheme.typography.titleLarge)
                    Text("${s.followers} followers", color = Color.Gray)
                    OutlinedButton(onClick = { attempt++ }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Reload")
                    }
                }
            }
            is ProfileState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { attempt++ }, modifier = Modifier.padding(top = 8.dp)) { Text("Retry") }
            }
        }
    }
}
// [/snippet]

// [snippet:co-flow]
// 冷流（cold flow）：有人 collect 才開始產生資料，停止收集就停止
private fun temperatureFlow(): Flow<Double> = flow {
    var t = 22.0
    while (true) {
        t += (22.0 - t) * 0.2 + Random.nextDouble(-0.8, 0.8)
        emit(t)
        delay(400)
    }
}

@Composable
fun FlowCollectDemo() {
    var listening by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Collect sensor", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = listening, onCheckedChange = { listening = it })
        }
        // TemperatureChart 離開組合時，collectAsState 會自動停止收集
        if (listening) TemperatureChart() else Text("Not collecting: the flow is stopped", color = Color.Gray)
    }
}

@Composable
private fun TemperatureChart() {
    val readings by remember {
        temperatureFlow().runningFold(emptyList<Double>()) { acc, t -> (acc + t).takeLast(20) }
    }.collectAsState(initial = emptyList())

    val latest = readings.lastOrNull()
    Text(
        if (latest == null) "Waiting..." else "${(latest * 10).roundToInt() / 10.0} °C",
        style = MaterialTheme.typography.displaySmall,
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(20 - readings.size) { Spacer(Modifier.weight(1f)) } // 空位：新資料從右邊長出來
        readings.forEach { t ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(((t - 17) / 9).toFloat().coerceIn(0.05f, 1f))
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }
    }
}
// [/snippet]

// [snippet:co-snapshot-flow]
@OptIn(FlowPreview::class)
@Composable
fun SnapshotFlowDemo() {
    var volume by remember { mutableFloatStateOf(0.5f) }
    var changes by remember { mutableIntStateOf(0) }
    val saves = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        snapshotFlow { volume } // 把 Compose 的 State 轉成 Flow
            .drop(1)            // 略過一開始的初始值
            .debounce(600)      // 停止拖動 600ms 後才發出
            .collect { v ->
                delay(200)      // 模擬寫入硬碟或後端
                saves.add(0, "Saved volume ${(v * 100).roundToInt()}%")
            }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Volume ${(volume * 100).roundToInt()}%", style = MaterialTheme.typography.titleMedium)
        Slider(value = volume, onValueChange = { volume = it; changes++ })
        Text(
            "Slider changes: $changes   Saves: ${saves.size}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
        )
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            saves.take(4).forEach { Text(it) }
        }
    }
}
// [/snippet]

// [snippet:co-callback]
// 假設這是第三方 SDK，只提供 callback / listener 形式的 API
data class Location(val lat: Double, val lng: Double)

fun interface Cancellable { fun cancel() }

class LocationClient {
    private val sdkScope = MainScope() // 模擬 SDK 內部的背景工作，實作細節不重要
    private val listeners = mutableListOf<(Location) -> Unit>()
    private var ticker: Job? = null
    var listenerCount by mutableIntStateOf(0) // 只是為了在畫面上顯示
        private set

    fun requestOnce(onResult: (Location) -> Unit, onError: (Throwable) -> Unit): Cancellable {
        val job = sdkScope.launch { delay(800); onResult(randomLocation()) }
        return Cancellable { job.cancel() }
    }

    fun addListener(listener: (Location) -> Unit) {
        listeners += listener
        listenerCount = listeners.size
        if (ticker == null) {
            ticker = sdkScope.launch { while (true) { delay(500); listeners.forEach { it(randomLocation()) } } }
        }
    }

    fun removeListener(listener: (Location) -> Unit) {
        listeners -= listener
        listenerCount = listeners.size
        if (listeners.isEmpty()) { ticker?.cancel(); ticker = null }
    }

    private fun randomLocation() =
        Location(25.0330 + Random.nextDouble(-0.002, 0.002), 121.5654 + Random.nextDouble(-0.002, 0.002))
}

// 一次性 callback → suspend 函式
suspend fun LocationClient.awaitLocation(): Location = suspendCancellableCoroutine { cont ->
    val request = requestOnce(
        onResult = { cont.resume(it) },
        onError = { cont.resumeWithException(it) },
    )
    cont.invokeOnCancellation { request.cancel() } // 協程被取消 → 一併取消 SDK 請求
}

// 持續性 listener → Flow
fun LocationClient.locationUpdates(): Flow<Location> = callbackFlow {
    val listener: (Location) -> Unit = { trySend(it) }
    addListener(listener)
    awaitClose { removeListener(listener) } // 停止 collect 時一定會執行，不會漏掉移除 listener
}

private fun Location.format(): String {
    fun Double.short() = (this * 10000).roundToInt() / 10000.0
    return "${lat.short()}, ${lng.short()}"
}

@Composable
fun CallbackBridgeDemo() {
    val client = remember { LocationClient() }
    val scope = rememberCoroutineScope()
    var once by remember { mutableStateOf("-") }
    var tracking by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = {
            scope.launch {
                once = "Locating..."
                once = client.awaitLocation().format() // 看起來就像一般的同步呼叫
            }
        }) { Text("Get location once") }
        Text(once)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Keep tracking", modifier = Modifier.weight(1f))
            Switch(checked = tracking, onCheckedChange = { tracking = it })
        }
        if (tracking) {
            val location by remember { client.locationUpdates() }.collectAsState(initial = null)
            Text(location?.format() ?: "Waiting for first update...")
        }
        Text(
            "Registered listeners: ${client.listenerCount}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
        )
    }
}
// [/snippet]

// [snippet:co-interop]
// 一個極簡的「LiveData」：保存一個值，值改變時通知所有 observer
class MiniLiveData<T>(initial: T) {
    private val observers = mutableListOf<(T) -> Unit>()
    var observerCount by mutableIntStateOf(0) // 只是為了在畫面上顯示
        private set

    var value: T = initial
        set(v) {
            field = v
            observers.forEach { it(v) }
        }

    fun observe(observer: (T) -> Unit) {
        observers += observer
        observerCount = observers.size
        observer(value) // 和 LiveData 一樣，一註冊就先收到目前的值
    }

    fun removeObserver(observer: (T) -> Unit) {
        observers -= observer
        observerCount = observers.size
    }
}

// observeAsState / subscribeAsState 的原理：進入組合時訂閱，離開組合時取消訂閱
@Composable
fun <T> MiniLiveData<T>.observeAsState(): State<T> {
    val state = remember { mutableStateOf(value) }
    DisposableEffect(this) {
        val observer: (T) -> Unit = { state.value = it }
        observe(observer)
        onDispose { removeObserver(observer) }
    }
    return state
}

@Composable
fun InteropDemo() {
    val score = remember { MiniLiveData(0) } // 實務上會放在 ViewModel 裡
    var showBadge by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { score.value++ }) { Text("Score +1") }
            OutlinedButton(onClick = { showBadge = !showBadge }) { Text(if (showBadge) "Hide" else "Show") }
        }
        if (showBadge) ScoreBadge(score)
        Text(
            "Active observers: ${score.observerCount}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
        )
    }
}

@Composable
private fun ScoreBadge(score: MiniLiveData<Int>) {
    val value by score.observeAsState()
    Card(Modifier.fillMaxWidth()) {
        Text("Score: $value", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
    }
}
// [/snippet]
