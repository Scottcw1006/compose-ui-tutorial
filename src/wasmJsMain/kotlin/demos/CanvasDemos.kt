package demos

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// [snippet:canvas-basics]
@Composable
fun CanvasBasicsDemo() {
    Canvas(Modifier.fillMaxSize().padding(24.dp)) {
        // size：畫布大小（px）；center：畫布中心點
        drawRoundRect(
            color = Color(0xFFF3EDF7),
            cornerRadius = CornerRadius(16.dp.toPx()),
        )
        drawLine(
            color = Color(0xFFCCC2DC),
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = 2.dp.toPx(),
        )
        drawCircle(
            color = Color(0xFF6750A4),
            radius = size.minDimension / 5,
            center = center,
        )
        val arcRadius = size.minDimension / 3
        drawArc(
            color = Color(0xFFB4589C),
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = center - Offset(arcRadius, arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}
// [/snippet]

// [snippet:canvas-path]
@Composable
fun CanvasPathDemo() {
    Canvas(Modifier.fillMaxSize()) {
        val outer = size.minDimension / 2.6f
        val inner = outer * 0.45f
        // 十個頂點交替使用外圈、內圈半徑，連起來就是星星
        val star = Path().apply {
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) outer else inner
                val angle = (-90 + i * 36) * PI / 180
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(
            path = star,
            brush = Brush.linearGradient(listOf(Color(0xFFFFC107), Color(0xFFE53935))),
        )
        drawPath(
            path = star,
            color = Color(0xFF5D4037),
            style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round),
        )
    }
}
// [/snippet]

// [snippet:canvas-ring]
@Composable
fun ProgressRingDemo() {
    var target by remember { mutableFloatStateOf(0.72f) }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                // 筆畫有寬度，要往內縮一半，否則會被裁掉
                val inset = stroke.width / 2
                val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
                drawArc(
                    color = Color(0xFFEADDFF),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = stroke,
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF6750A4), Color(0xFFB4589C), Color(0xFF6750A4))
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = stroke,
                )
            }
            Text("${(progress * 100).roundToInt()}%", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { target = Random.nextFloat() }) { Text("Randomize") }
    }
}
// [/snippet]

// [snippet:canvas-drawing]
@Composable
fun DrawingPadDemo() {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var current by remember { mutableStateOf(listOf<Offset>()) }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { current = listOf(it) },
                        onDragEnd = { strokes += current; current = emptyList() },
                        onDragCancel = { current = emptyList() },
                    ) { change, _ ->
                        change.consume()
                        current = current + change.position
                    }
                }
        ) {
            (strokes + listOf(current)).forEach { points ->
                if (points.size < 2) return@forEach
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = path,
                    color = Color(0xFF6750A4),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
        if (strokes.isEmpty() && current.isEmpty()) {
            Text("Draw here", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
        }
        TextButton(onClick = { strokes.clear() }, modifier = Modifier.align(Alignment.TopEnd)) {
            Text("Clear")
        }
    }
}
// [/snippet]

// [snippet:canvas-draw-modifier]
@Composable
fun DrawModifierDemo() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // drawBehind：畫在內容「後面」，像螢光筆
        Text(
            "Highlighted text",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.drawBehind {
                drawRect(
                    color = Color(0xFFFFE082),
                    topLeft = Offset(0f, size.height * 0.55f),
                    size = Size(size.width, size.height * 0.35f),
                )
            },
        )
        // drawWithContent：自己決定內容與裝飾誰先畫，這裡把紅點畫在最上層
        Box(
            Modifier
                .size(72.dp)
                .background(Color(0xFFEADDFF), RoundedCornerShape(16.dp))
                .drawWithContent {
                    drawContent()
                    drawCircle(
                        color = Color(0xFFE53935),
                        radius = 8.dp.toPx(),
                        center = Offset(size.width - 6.dp.toPx(), 6.dp.toPx()),
                    )
                },
            contentAlignment = Alignment.Center,
        ) { Text("Inbox") }
        // drawWithCache：Brush 依尺寸建立，只在尺寸改變時重建，重畫時直接沿用
        Box(
            Modifier
                .size(width = 200.dp, height = 56.dp)
                .drawWithCache {
                    val brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6750A4), Color(0xFFB4589C)),
                        startX = 0f,
                        endX = size.width,
                    )
                    val corner = CornerRadius(12.dp.toPx())
                    onDrawBehind {
                        drawRoundRect(
                            brush = brush,
                            cornerRadius = corner,
                            style = Stroke(width = 3.dp.toPx()),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) { Text("Gradient border") }
    }
}
// [/snippet]
