package com.example.migarage.ui.maintenance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMaintenanceScreen(
    carId: String,
    maintId: String?,               // null -> crear, no null -> editar
    onDone: () -> Unit,
    vm: AddEditMaintenanceViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val isEdit = maintId != null

    var type by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var km by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    // Cargar datos si es edición
    LaunchedEffect(maintId) {
        if (isEdit) {
            isBusy = true
            vm.load(carId, maintId!!)?.let {
                type = it.type
                dateMillis = it.dateMillis
                km = it.km.toString()
                cost = if (it.cost == 0.0) "" else it.cost.toString()
                notes = it.notes
            }
            isBusy = false
        }
    }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isEdit) "Editar mantenimiento" else "Nuevo mantenimiento") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                OutlinedTextField(
                    value = type, onValueChange = { type = it },
                    label = { Text("Tipo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fmt.format(Date(dateMillis)),
                    onValueChange = {},
                    label = { Text("Fecha") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isBusy) { showDate = true },
                    readOnly = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = km,
                    onValueChange = { if (it.all(Char::isDigit)) km = it },
                    label = { Text("Kilómetros") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = cost,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("""\d+(\.\d{0,2})?"""))) cost = it
                    },
                    label = { Text("Coste (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    enabled = !isBusy,
                    onClick = {
                        val err = when {
                            type.isBlank() -> "El tipo es obligatorio"
                            (km.toIntOrNull() ?: -1) < 0 -> "Kilómetros inválidos"
                            else -> null
                        }
                        if (err != null) {
                            scope.launch { snackbar.showSnackbar(err) }
                            return@Button
                        }

                        isBusy = true
                        scope.launch {
                            val ok = if (isEdit)
                                vm.saveEdit(
                                    carId, maintId!!, type,
                                    dateMillis, km.toInt(),
                                    cost.toDoubleOrNull() ?: 0.0, notes
                                )
                            else
                                vm.saveNew(
                                    carId, type, dateMillis,
                                    km.toInt(), cost.toDoubleOrNull() ?: 0.0, notes
                                )
                            isBusy = false
                            if (ok) onDone() else snackbar.showSnackbar("No se pudo guardar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (isEdit) "Guardar cambios" else "Añadir mantenimiento") }
            }

            if (isBusy) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = state.selectedDateMillis ?: System.currentTimeMillis()
                    dateMillis = selected
                    showDate = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }
}
