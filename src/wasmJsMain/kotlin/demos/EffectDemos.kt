package demos

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// [snippet:fx-layer]
@Composable
fun GraphicsLayerDemo() {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var opacity by remember { mutableFloatStateOf(1f) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                        alpha = opacity
                    }
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFFB4589C))),
                        RoundedCornerShape(20.dp),
                    )
            )
        }
        LabeledSlider("scale", scale, 0.5f..1.5f) { scale = it }
        LabeledSlider("rotationZ", rotation, 0f..360f) { rotation = it }
        LabeledSlider("alpha", opacity, 0f..1f) { opacity = it }
    }
}

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, modifier = Modifier.width(76.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
    }
}
// [/snippet]

// [snippet:fx-flip]
@Composable
fun FlipCardDemo() {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600),
    )
    val showFront = rotation <= 90f

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(width = 220.dp, height = 140.dp)
                .graphicsLayer {
                    rotationY = rotation
                    // 拉遠相機距離，否則翻轉時透視變形會太誇張
                    cameraDistance = 12f * density
                }
                .clip(RoundedCornerShape(16.dp))
                .background(if (showFront) Color(0xFF6750A4) else Color(0xFF7D5260))
                .clickable { flipped = !flipped },
            contentAlignment = Alignment.Center,
        ) {
            if (showFront) {
                Text("FRONT", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                // 背面再轉 180 度，文字才不會左右相反
                Text(
                    "BACK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                )
            }
        }
    }
}
// [/snippet]

// [snippet:fx-shimmer]
private val ShimmerBase = Color(0xFFE7E0EC)
private val ShimmerHighlight = Color(0xFFF7F2FA)

// 傳入 lambda：動畫值只在繪製階段被讀取，每一幀只重畫、不重組
fun Modifier.shimmer(progress: () -> Float): Modifier = drawBehind {
    val x = -size.width + 3 * size.width * progress()
    drawRect(
        Brush.linearGradient(
            colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
            start = Offset(x, 0f),
            end = Offset(x + size.width, size.height),
        )
    )
}

@Composable
fun ShimmerDemo() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "progress",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        repeat(4) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).shimmer { progress })
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.fillMaxWidth(0.7f).height(14.dp)
                            .clip(RoundedCornerShape(4.dp)).shimmer { progress }
                    )
                    Box(
                        Modifier.fillMaxWidth(0.45f).height(14.dp)
                            .clip(RoundedCornerShape(4.dp)).shimmer { progress }
                    )
                }
            }
        }
    }
}
// [/snippet]

// [snippet:fx-spotlight]
@Composable
fun SpotlightDemo() {
    var pointer by remember { mutableStateOf(Offset.Unspecified) }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 低階 API：自己處理每一個指標事件（包含滑鼠懸停移動）
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        pointer = if (event.type == PointerEventType.Exit) {
                            Offset.Unspecified
                        } else {
                            event.changes.first().position
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFF48FB1)))),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("You found it!", style = MaterialTheme.typography.headlineSmall)
            Text("Hidden under the overlay")
        }
        // 遮罩層：離屏合成後用 BlendMode.DstOut 挖出一個柔邊的洞
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawRect(Color(0xFF121016))
                    if (pointer.isSpecified) {
                        val radius = 72.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.Black, Color.Transparent),
                                center = pointer,
                                radius = radius,
                            ),
                            radius = radius,
                            center = pointer,
                            blendMode = BlendMode.DstOut,
                        )
                    }
                }
        )
        if (!pointer.isSpecified) {
            Text(
                "Move your pointer here",
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            )
        }
    }
}
// [/snippet]

// [snippet:fx-blur]
@Composable
fun BlurDemo() {
    var radius by remember { mutableFloatStateOf(0f) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            Modifier.fillMaxWidth().weight(1f).blur(radius.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(140.dp)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color(0xFF6750A4), Color(0xFFF48FB1), Color(0xFFFFC107), Color(0xFF6750A4))
                        ),
                        CircleShape,
                    )
            )
            Text(
                "Compose",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("blur ${radius.roundToInt()}dp", fontSize = 13.sp, modifier = Modifier.width(76.dp))
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 0f..20f,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
// [/snippet]
