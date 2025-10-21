package com.example.migarage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    valueMillis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = fmt.format(Date(valueMillis)),
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled) { showPicker = true },
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showPicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Elegir fecha")
            }
        },
        visualTransformation = VisualTransformation.None
    )

    if (showPicker) {
        // Estado del calendario con la fecha actual precargada
        val state = rememberDatePickerState(
            initialSelectedDateMillis = valueMillis
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = state.selectedDateMillis ?: valueMillis // si no tocan, conserva
                    onChange(selected)
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            Column(Modifier.padding(8.dp)) {
                DatePicker(state = state)
                Spacer(Modifier.height(4.dp))
                // Mini resumen de selección
                val text = state.selectedDateMillis?.let { fmt.format(Date(it)) } ?: fmt.format(Date(valueMillis))
                Text(
                    text = "Seleccionado: $text",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 12.dp, bottom = 4.dp)
                )
            }
        }
    }
}
