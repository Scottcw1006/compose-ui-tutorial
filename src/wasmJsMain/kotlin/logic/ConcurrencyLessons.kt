package logic

import androidx.compose.runtime.Composable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// [snippet:logic-channel]
suspend fun packOrders(capacity: Int, log: (String) -> Unit) = coroutineScope {
    val orders = Channel<Int>(capacity)

    launch {
        for (id in 1..6) {
            orders.send(id) // 緩衝區滿了（或沒有緩衝）就在這裡暫停，等 worker 取走
            log("[producer] sent #$id")
        }
        orders.close() // 關閉後，worker 收完剩下的訂單，for 迴圈就會結束
        log("[producer] closed")
    }

    repeat(2) { n ->
        launch {
            for (id in orders) { // 每筆訂單只會被一個 worker 取走
                log("[worker ${n + 1}] packing #$id")
                delay(600)
            }
        }
    }
}
// [/snippet]

@Composable
fun ChannelDemo() = ConsoleDemo(listOf("RENDEZVOUS (0)", "capacity = 4")) { option, log ->
    packOrders(if (option == 0) Channel.RENDEZVOUS else 4, log)
}

// [snippet:logic-mutex]
class TicketCounter {
    private val mutex = Mutex()
    var sold = 0
        private set

    // 讀取 → 暫停（例如寫資料庫）→ 寫回：暫停期間，其他協程讀到的也是同一個舊值
    suspend fun sellUnsafe() {
        val current = sold
        delay(10)
        sold = current + 1
    }

    // withLock 讓「讀取到寫回」變成一個整體，同一時間只有一個協程能進來
    suspend fun sellSafe() = mutex.withLock { sellUnsafe() }
}

suspend fun sellTickets(safe: Boolean, log: (String) -> Unit) {
    val counter = TicketCounter()
    log("[box office] 100 coroutines, each sells 1 ticket")
    coroutineScope {
        repeat(100) {
            launch { if (safe) counter.sellSafe() else counter.sellUnsafe() }
        }
    }
    log("[box office] sold = ${counter.sold}")
}
// [/snippet]

@Composable
fun MutexDemo() = ConsoleDemo(listOf("no lock", "Mutex")) { option, log ->
    sellTickets(safe = option == 1, log)
}
