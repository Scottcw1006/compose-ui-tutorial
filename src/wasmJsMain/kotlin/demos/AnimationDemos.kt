package demos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// [snippet:anim-as-state]
@Composable
fun AnimateAsStateDemo() {
    var big by remember { mutableStateOf(false) }
    // 目標值一改變，就自動從目前值動畫過去
    val size by animateDpAsState(
        targetValue = if (big) 160.dp else 80.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    )
    val color by animateColorAsState(
        targetValue = if (big) Color(0xFFB4589C) else Color(0xFF6750A4),
        animationSpec = tween(durationMillis = 500),
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size)
                .clip(RoundedCornerShape(24.dp))
                .background(color)
                .clickable { big = !big },
            contentAlignment = Alignment.Center,
        ) { Text("Tap", color = Color.White) }
    }
}
// [/snippet]

// [snippet:anim-visibility]
@Composable
fun AnimatedVisibilityDemo() {
    var visible by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "Hide" else "Show")
        }
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Text(
                    "I fade and expand in, then shrink away.",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        Text("Content below moves smoothly", modifier = Modifier.padding(top = 12.dp))
    }
}
// [/snippet]

// [snippet:anim-content]
@Composable
fun AnimatedContentDemo() {
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                // 往上加：新數字從下方滑入；往下減：從上方滑入
                if (targetState > initialState) {
                    (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                } else {
                    (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
                }
            },
        ) { value ->
            Text("$value", style = MaterialTheme.typography.displayLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { count-- }) { Text("-1") }
            Button(onClick = { count++ }) { Text("+1") }
        }
    }
}
// [/snippet]

// [snippet:anim-transition]
@Composable
fun TransitionDemo() {
    var selected by remember { mutableStateOf(false) }
    // 一個狀態驅動多個屬性，所有動畫同步開始、同步結束
    val transition = updateTransition(targetState = selected, label = "card")
    val corner by transition.animateDp(label = "corner") { if (it) 48.dp else 12.dp }
    val padding by transition.animateDp(label = "padding") { if (it) 32.dp else 16.dp }
    val bg by transition.animateColor(label = "bg") {
        if (it) Color(0xFF6750A4) else Color(0xFFEADDFF)
    }
    val fg by transition.animateColor(label = "fg") {
        if (it) Color.White else Color(0xFF21005D)
    }

    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(corner))
                .background(bg)
                .clickable { selected = !selected },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (selected) "Selected" else "Tap to select", color = fg, fontWeight = FontWeight.Bold)
        }
    }
}
// [/snippet]

// [snippet:anim-content-size]
@Composable
fun AnimateContentSizeDemo() {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            Modifier
                .fillMaxWidth()
                .animateContentSize() // 尺寸改變時自動補間
                .clickable { expanded = !expanded },
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("What is Compose?", fontWeight = FontWeight.Bold)
                Text(
                    text = if (expanded) {
                        "Jetpack Compose is Android's modern toolkit for building " +
                            "native UI. It simplifies and accelerates UI development " +
                            "with less code, powerful tools, and intuitive Kotlin APIs."
                    } else {
                        "Tap to read more..."
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
// [/snippet]

// [snippet:anim-infinite]
@Composable
fun InfiniteDemo() {
    val transition = rememberInfiniteTransition(label = "loader")
    val scale by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "rotation",
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(96.dp)
                // graphicsLayer 只影響繪製，不會觸發重新排版，效能最好
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
                .background(Color(0xFF6750A4), RoundedCornerShape(20.dp))
        )
        Box(Modifier.size(16.dp).background(Color.White, CircleShape))
    }
}
// [/snippet]
