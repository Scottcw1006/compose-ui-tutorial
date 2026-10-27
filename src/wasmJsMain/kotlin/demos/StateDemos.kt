package demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// [snippet:state-counter]
@Composable
fun CounterDemo() {
    // remember：重組時保留值；mutableStateOf：值改變時觸發重組
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$count", style = MaterialTheme.typography.displayMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { count-- }) { Text("-1") }
            Button(onClick = { count++ }) { Text("+1") }
        }
    }
}
// [/snippet]

// [snippet:state-textfield]
@Composable
fun TextFieldDemo() {
    var name by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = if (name.isBlank()) "Type something above" else "Hello, $name!",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text("${name.length} characters", color = MaterialTheme.colorScheme.outline)
    }
}
// [/snippet]

// [snippet:state-hoisting]
// 無狀態元件：只負責顯示，事件往上丟
@Composable
fun Stepper(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$label: $value", modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { onValueChange(value - 1) }) { Text("-") }
        OutlinedButton(onClick = { onValueChange(value + 1) }) { Text("+") }
    }
}

// 有狀態的父層：持有狀態，可以把資料組合起來
@Composable
fun CartDemo() {
    var apples by remember { mutableIntStateOf(1) }
    var oranges by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Stepper("Apples", apples) { apples = it.coerceAtLeast(0) }
        Stepper("Oranges", oranges) { oranges = it.coerceAtLeast(0) }
        Text(
            "Total: ${apples + oranges} items",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
// [/snippet]
