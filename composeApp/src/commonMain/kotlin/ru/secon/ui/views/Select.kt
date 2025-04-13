package ru.secon.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Checkable<T>(
    val type: T,
    val text: String,
    val checked: Boolean
)

@Composable
fun <T> SelectList(
    items: List<Checkable<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { Radio(it, onSelect) }
    }
}

@Composable
fun <T> Radio(
    item: Checkable<T>,
    onSelect: (T) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.clickable { onSelect(item.type) }.fillMaxWidth()
    ) {
        RadioButton(
            onClick = { onSelect(item.type) },
            selected = item.checked
        )
        Text(item.text, modifier = Modifier.align(Alignment.CenterVertically))
    }
}