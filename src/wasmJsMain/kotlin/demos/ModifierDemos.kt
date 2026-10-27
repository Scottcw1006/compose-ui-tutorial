package demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// [snippet:modifier-order]
@Composable
fun ModifierOrderDemo() {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 先 padding 再 background：留白在色塊「外面」
        Text(
            "padding → bg",
            modifier = Modifier
                .padding(16.dp)
                .background(Color(0xFFD0BCFF)),
        )
        // 先 background 再 padding：留白在色塊「裡面」
        Text(
            "bg → padding",
            modifier = Modifier
                .background(Color(0xFFD0BCFF))
                .padding(16.dp),
        )
    }
}
// [/snippet]

// [snippet:modifier-size]
@Composable
fun ModifierSizeDemo() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(1f, 0.75f, 0.5f, 0.25f).forEach { fraction ->
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(36.dp)
                    .background(Color(0xFF6750A4), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    "fillMaxWidth($fraction)",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Box(Modifier.size(width = 120.dp, height = 36.dp).background(Color(0xFF7D5260))) {
            Text(
                "size(120, 36)",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
// [/snippet]

// [snippet:modifier-clickable]
@Composable
fun ModifierClickableDemo() {
    var count by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 120.dp)
                .clip(RoundedCornerShape(16.dp)) // 先 clip，水波紋才會被裁成圓角
                .background(Color(0xFFEADDFF))
                .clickable { count++ },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tap me", fontWeight = FontWeight.Bold)
                Text("clicked $count times", color = Color.Gray)
            }
        }
    }
}
// [/snippet]

// [snippet:modifier-shape]
@Composable
fun ModifierShapeDemo() {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .background(Color(0xFF6750A4), CircleShape)
        )
        Box(
            Modifier
                .size(72.dp)
                .border(3.dp, Color(0xFF6750A4), RoundedCornerShape(16.dp))
        )
        Box(
            Modifier
                .size(72.dp)
                .clip(CutCornerShape(topStart = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFF7D5260))
        )
    }
}
// [/snippet]
