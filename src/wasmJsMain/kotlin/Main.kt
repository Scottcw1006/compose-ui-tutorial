import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import demos.AnimateAsStateDemo
import demos.AnimateContentSizeDemo
import demos.AnimatedContentDemo
import demos.AnimatedVisibilityDemo
import demos.ArrangementDemo
import demos.BlurDemo
import demos.BoxDemo
import demos.CanvasBasicsDemo
import demos.CanvasPathDemo
import demos.CartDemo
import demos.ColumnDemo
import demos.CounterDemo
import demos.DragGestureDemo
import demos.DrawModifierDemo
import demos.DrawingPadDemo
import demos.FlipCardDemo
import demos.GraphicsLayerDemo
import demos.InfiniteDemo
import demos.LazyColumnDemo
import demos.LazyGridDemo
import demos.LazyRowDemo
import demos.ModifierClickableDemo
import demos.ModifierOrderDemo
import demos.ModifierShapeDemo
import demos.ModifierSizeDemo
import demos.ProgressRingDemo
import demos.RowDemo
import demos.ScrollToTopDemo
import demos.ShimmerDemo
import demos.SpotlightDemo
import demos.SwipeToDismissDemo
import demos.TapGestureDemo
import demos.TextFieldDemo
import demos.TransitionDemo
import demos.WeightDemo
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

// 網頁上 data-demo="<id>" 對應到的 Composable
private val demos: Map<String, @Composable () -> Unit> = mapOf(
    "layout-column" to { ColumnDemo() },
    "layout-row" to { RowDemo() },
    "layout-box" to { BoxDemo() },
    "layout-arrangement" to { ArrangementDemo() },
    "layout-weight" to { WeightDemo() },
    "modifier-order" to { ModifierOrderDemo() },
    "modifier-size" to { ModifierSizeDemo() },
    "modifier-clickable" to { ModifierClickableDemo() },
    "modifier-shape" to { ModifierShapeDemo() },
    "state-counter" to { CounterDemo() },
    "state-textfield" to { TextFieldDemo() },
    "state-hoisting" to { CartDemo() },
    "lists-column" to { LazyColumnDemo() },
    "lists-row" to { LazyRowDemo() },
    "lists-grid" to { LazyGridDemo() },
    "lists-scroll-state" to { ScrollToTopDemo() },
    "anim-as-state" to { AnimateAsStateDemo() },
    "anim-visibility" to { AnimatedVisibilityDemo() },
    "anim-content" to { AnimatedContentDemo() },
    "anim-transition" to { TransitionDemo() },
    "anim-content-size" to { AnimateContentSizeDemo() },
    "anim-infinite" to { InfiniteDemo() },
    "gesture-tap" to { TapGestureDemo() },
    "gesture-drag" to { DragGestureDemo() },
    "gesture-swipe" to { SwipeToDismissDemo() },
    "canvas-basics" to { CanvasBasicsDemo() },
    "canvas-path" to { CanvasPathDemo() },
    "canvas-ring" to { ProgressRingDemo() },
    "canvas-drawing" to { DrawingPadDemo() },
    "canvas-draw-modifier" to { DrawModifierDemo() },
    "fx-layer" to { GraphicsLayerDemo() },
    "fx-flip" to { FlipCardDemo() },
    "fx-shimmer" to { ShimmerDemo() },
    "fx-spotlight" to { SpotlightDemo() },
    "fx-blur" to { BlurDemo() },
)

@Composable
private fun DemoHost(id: String) {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            val demo = demos[id]
            if (demo != null) demo() else Text("Unknown demo: $id")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val nodes = document.querySelectorAll("[data-demo]")
    for (i in 0 until nodes.length) {
        val el = nodes.item(i) as HTMLElement
        val id = el.getAttribute("data-demo") ?: continue
        ComposeViewport(el) { DemoHost(id) }
        el.setAttribute("data-ready", "true")
    }
}
