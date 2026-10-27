package demos

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

// [snippet:gesture-tap]
@Composable
fun TapGestureDemo() {
    var event by remember { mutableStateOf("Tap, double tap or long press") }
    var position by remember { mutableStateOf(Offset.Unspecified) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3EDF7))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { event = "Tap"; position = it },
                    onDoubleTap = { event = "Double tap"; position = it },
                    onLongPress = { event = "Long press"; position = it },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(event, style = MaterialTheme.typography.titleMedium)
        if (position.isSpecified) {
            val radius = 16.dp
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            (position.x - radius.toPx()).roundToInt(),
                            (position.y - radius.toPx()).roundToInt(),
                        )
                    }
                    .size(radius * 2)
                    .background(Color(0x806750A4), CircleShape)
            )
        }
    }
}
// [/snippet]

// [snippet:gesture-drag]
@Composable
fun DragGestureDemo() {
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                // lambda 版的 offset 在排版階段才讀值，拖曳時不會觸發重組
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .size(88.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(Color(0xFF6750A4), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                },
            contentAlignment = Alignment.Center,
        ) { Text("Drag me", color = Color.White) }

        TextButton(
            onClick = { offset = Offset.Zero },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { Text("Reset") }
    }
}
// [/snippet]

// [snippet:gesture-swipe]
@Composable
fun SwipeToDismissDemo() {
    var messages by remember { mutableStateOf(List(4) { "Message ${it + 1}" }) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        messages.forEach { message ->
            // key 讓每一列的 offsetX 狀態跟著資料走，刪除後不會錯位
            key(message) {
                SwipeableRow(message, onDismiss = { messages = messages - message })
            }
        }
        if (messages.isEmpty()) {
            TextButton(onClick = { messages = List(4) { "Message ${it + 1}" } }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun SwipeableRow(text: String, onDismiss: () -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var width by remember { mutableFloatStateOf(1f) }

    Box(
        Modifier
            .fillMaxWidth()
            .onSizeChanged { width = it.width.toFloat() }
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer { alpha = 1f - (abs(offsetX) / width).coerceIn(0f, 1f) }
            .background(Color(0xFFEADDFF), RoundedCornerShape(12.dp))
            .draggable(
                orientation = Orientation.Horizontal,
                // 拖曳中：直接跟手
                state = rememberDraggableState { delta -> offsetX += delta },
                onDragStopped = { velocity ->
                    // 拖超過三分之一，或甩得夠快，就飛出去；否則彈回原位
                    val dismiss = abs(offsetX) > width / 3 || abs(velocity) > 1500f
                    val direction = sign(if (offsetX != 0f) offsetX else velocity)
                    animate(
                        initialValue = offsetX,
                        targetValue = if (dismiss) direction * width else 0f,
                        initialVelocity = velocity,
                        animationSpec = spring(),
                    ) { value, _ -> offsetX = value }
                    if (dismiss) onDismiss()
                },
            )
            .padding(16.dp)
    ) {
        Text(text)
    }
}
// [/snippet]
