package demos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// [snippet:lists-column]
data class Contact(val id: Int, val name: String)

@Composable
fun LazyColumnDemo() {
    val contacts = remember { List(1000) { Contact(it, "Contact #${it + 1}") } }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(
                "1000 contacts",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
        }
        // key 讓 Compose 在資料變動時正確追蹤每一列
        items(contacts, key = { it.id }) { contact ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).background(Color(0xFFEADDFF), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("${contact.id % 10}") }
                Spacer(Modifier.width(12.dp))
                Text(contact.name)
            }
            HorizontalDivider()
        }
    }
}
// [/snippet]

// [snippet:lists-row]
@Composable
fun LazyRowDemo() {
    val colors = listOf(0xFF6750A4, 0xFF7D5260, 0xFF625B71, 0xFF386A20, 0xFF00639B)

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("Featured", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(20) { index ->
                Card(Modifier.size(width = 120.dp, height = 140.dp)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(colors[index % colors.size])),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Text(
                            "Item ${index + 1}",
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}
// [/snippet]

// [snippet:lists-grid]
@Composable
fun LazyGridDemo() {
    var adaptive by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !adaptive,
                onClick = { adaptive = false },
                label = { Text("Fixed(3)") },
            )
            FilterChip(
                selected = adaptive,
                onClick = { adaptive = true },
                label = { Text("Adaptive(56.dp)") },
            )
        }
        LazyVerticalGrid(
            columns = if (adaptive) {
                GridCells.Adaptive(minSize = 56.dp) // 每欄至少 56dp，欄數依寬度決定
            } else {
                GridCells.Fixed(3) // 不論寬度，固定 3 欄
            },
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items((1..60).toList()) { n ->
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .background(
                            Color.hsv(hue = (n * 6f) % 360f, saturation = 0.45f, value = 0.95f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) { Text("$n", fontSize = 16.sp) }
            }
        }
    }
}
// [/snippet]

// [snippet:lists-scroll-state]
@Composable
fun ScrollToTopDemo() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // derivedStateOf：只有在「是否捲過 Row 5」這個布林值改變時才觸發重組，
    // 而不是每次 firstVisibleItemIndex 變動都重組
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(100) { index ->
                Text("Row $index", modifier = Modifier.fillMaxWidth().padding(16.dp))
                HorizontalDivider()
            }
        }
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
            ) { Text("Top") }
        }
    }
}
// [/snippet]
