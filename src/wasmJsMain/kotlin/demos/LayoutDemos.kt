package demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// [snippet:layout-column]
@Composable
fun ColumnDemo() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
        Text("Column stacks children vertically", color = Color.Gray)
        Button(onClick = { }) {
            Text("Get started")
        }
    }
}
// [/snippet]

// [snippet:layout-row]
@Composable
fun RowDemo() {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF6750A4))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Jetpack Compose", fontWeight = FontWeight.Bold)
            Text("Row + Column = list item", color = Color.Gray, fontSize = 13.sp)
        }
        Checkbox(checked = checked, onCheckedChange = { checked = it })
    }
}
// [/snippet]

// [snippet:layout-box]
@Composable
fun BoxDemo() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFFB4589C)))
                )
        ) {
            Text(
                "Center",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
            )
            Text(
                "BottomStart",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            )
        }
        // 後宣告的子元件會疊在上層
        Box(
            Modifier
                .offset(x = 72.dp, y = (-72).dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935)),
            contentAlignment = Alignment.Center,
        ) {
            Text("3", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
// [/snippet]

// [snippet:layout-arrangement]
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArrangementDemo() {
    val options: List<Pair<String, Arrangement.Horizontal>> = listOf(
        "Start" to Arrangement.Start,
        "Center" to Arrangement.Center,
        "End" to Arrangement.End,
        "SpaceBetween" to Arrangement.SpaceBetween,
        "SpaceAround" to Arrangement.SpaceAround,
        "SpaceEvenly" to Arrangement.SpaceEvenly,
    )
    var selected by remember { mutableIntStateOf(0) }
    val colors = listOf(Color(0xFF6750A4), Color(0xFF7D5260), Color(0xFF625B71))

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp)),
            horizontalArrangement = options[selected].second,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            colors.forEach { color ->
                Box(Modifier.size(44.dp).background(color, RoundedCornerShape(8.dp)))
            }
        }
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, (name, _) ->
                FilterChip(
                    selected = index == selected,
                    onClick = { selected = index },
                    label = { Text(name) },
                )
            }
        }
    }
}
// [/snippet]

// [snippet:layout-weight]
@Composable
fun WeightDemo() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth().height(56.dp)) {
            Box(
                Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD0BCFF)),
                contentAlignment = Alignment.Center,
            ) { Text("weight 1f") }
            Box(
                Modifier.weight(2f).fillMaxHeight().background(Color(0xFFEFB8C8)),
                contentAlignment = Alignment.Center,
            ) { Text("weight 2f") }
            Box(
                Modifier.width(64.dp).fillMaxHeight().background(Color(0xFFCCC2DC)),
                contentAlignment = Alignment.Center,
            ) { Text("64dp") }
        }
        // 用 weight 撐開剩餘空間，把按鈕推到底部
        Spacer(Modifier.weight(1f))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Pinned to bottom")
        }
    }
}
// [/snippet]
