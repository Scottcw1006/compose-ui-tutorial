import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import demos.AnimatableDemo
import demos.AnimateAsStateDemo
import demos.AnimateContentSizeDemo
import demos.AnimatedContentDemo
import demos.AnimatedVisibilityDemo
import demos.ArrangementDemo
import demos.BlurDemo
import demos.BoxDemo
import demos.CanvasBasicsDemo
import demos.CanvasPathDemo
import demos.CallbackBridgeDemo
import demos.CartDemo
import demos.ColumnDemo
import demos.CounterDemo
import demos.DebouncedSearchDemo
import demos.DragGestureDemo
import demos.DrawModifierDemo
import demos.DrawingPadDemo
import demos.FlipCardDemo
import demos.FlowCollectDemo
import demos.GraphicsLayerDemo
import demos.InfiniteDemo
import demos.InteropDemo
import demos.LazyColumnDemo
import demos.LazyGridDemo
import demos.LazyRowDemo
import demos.ModifierClickableDemo
import demos.ModifierOrderDemo
import demos.ModifierShapeDemo
import demos.ModifierSizeDemo
import demos.ParallelDemo
import demos.ProduceStateDemo
import demos.ProgressRingDemo
import demos.RowDemo
import demos.ScopeLaunchDemo
import demos.ScrollToTopDemo
import demos.ShimmerDemo
import demos.SnapshotFlowDemo
import demos.SpotlightDemo
import demos.SwipeToDismissDemo
import demos.TapGestureDemo
import demos.TextFieldDemo
import demos.TransitionDemo
import demos.WeightDemo
import kotlinx.browser.document
import logic.BackpressureDemo
import logic.BuildersDemo
import logic.CancelDemo
import logic.ChannelDemo
import logic.CombineDemo
import logic.ContextDemo
import logic.HotFlowDemo
import logic.JobTreeDemo
import logic.MutexDemo
import logic.PropagationDemo
import logic.SupervisorDemo
import logic.SuspendDemo
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
    "co-launched-effect" to { DebouncedSearchDemo() },
    "co-scope" to { ScopeLaunchDemo() },
    "co-parallel" to { ParallelDemo() },
    "co-produce-state" to { ProduceStateDemo() },
    "co-flow" to { FlowCollectDemo() },
    "co-snapshot-flow" to { SnapshotFlowDemo() },
    "co-callback" to { CallbackBridgeDemo() },
    "co-interop" to { InteropDemo() },
    "anim-as-state" to { AnimateAsStateDemo() },
    "anim-visibility" to { AnimatedVisibilityDemo() },
    "anim-content" to { AnimatedContentDemo() },
    "anim-transition" to { TransitionDemo() },
    "anim-animatable" to { AnimatableDemo() },
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
    // 延伸閱讀：Coroutine 邏輯層實戰
    "logic-suspend" to { SuspendDemo() },
    "logic-builders" to { BuildersDemo() },
    "logic-job-tree" to { JobTreeDemo() },
    "logic-cancel" to { CancelDemo() },
    "logic-propagation" to { PropagationDemo() },
    "logic-supervisor" to { SupervisorDemo() },
    "logic-context" to { ContextDemo() },
    "logic-backpressure" to { BackpressureDemo() },
    "logic-combine" to { CombineDemo() },
    "logic-hot-flow" to { HotFlowDemo() },
    "logic-channel" to { ChannelDemo() },
    "logic-mutex" to { MutexDemo() },
)

@Composable
private fun DemoHost(id: String) {
    // 邏輯層的範例是深色的執行紀錄面板
    val colors = if (id.startsWith("logic-")) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors) {
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
