package com.example.migarage.ui.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.migarage.notify.ReminderScheduler
import com.example.migarage.ui.components.DateField
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMaintenanceScreen(
    carId: String,
    maintId: String?,               // null -> crear, no null -> editar
    onDone: () -> Unit,
    vm: AddEditMaintenanceViewModel = viewModel()
) {
    // Helpers/estado general
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isEdit = maintId != null

    var type by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var km by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var remind by remember { mutableStateOf(false) } // toggle recordatorio

    // Carga de datos si edita
    LaunchedEffect(maintId) {
        if (isEdit) {
            isBusy = true
            vm.load(carId, maintId!!)?.let {
                type = it.type
                dateMillis = it.dateMillis
                km = it.km.toString()
                cost = if (it.cost == 0.0) "" else it.cost.toString()
                notes = it.notes
                remind = it.dateMillis > System.currentTimeMillis()
            }
            isBusy = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEdit) "Editar mantenimiento" else "Nuevo mantenimiento") })
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {

                // Tipo
                OutlinedTextField(
                    value = type, onValueChange = { type = it },
                    label = { Text("Tipo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )

                Spacer(Modifier.height(8.dp))

                // Fecha con calendario desplegable (Material 3 DatePicker en diálogo)
                DateField(
                    label = "Fecha",
                    valueMillis = dateMillis,
                    onChange = { newMillis -> dateMillis = newMillis },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy
                )

                // Toggle recordatorio
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recordarme en esta fecha")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = remind, onCheckedChange = { remind = it }, enabled = !isBusy)
                }

                Spacer(Modifier.height(8.dp))

                // Km
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

                // Coste
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

                // Notas
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))

                // Acciones
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                val ok = if (isEdit) {
                                    val r = vm.saveEdit(
                                        carId, maintId!!, type,
                                        dateMillis, km.toInt(),
                                        cost.toDoubleOrNull() ?: 0.0, notes
                                    )
                                    if (r) {
                                        if (remind) {
                                            ReminderScheduler.schedule(
                                                context, carId, maintId,
                                                dateMillisAtNine(dateMillis),
                                                "Mantenimiento: $type",
                                                "Hoy toca $type del coche"
                                            )
                                        } else {
                                            ReminderScheduler.cancel(context, carId, maintId)
                                        }
                                    }
                                    r
                                } else {
                                    val createdId = vm.saveNewReturnId(
                                        carId, type, dateMillis, km.toInt(),
                                        cost.toDoubleOrNull() ?: 0.0, notes
                                    )
                                    if (createdId != null && remind) {
                                        ReminderScheduler.schedule(
                                            context, carId, createdId,
                                            dateMillisAtNine(dateMillis),
                                            "Mantenimiento: $type",
                                            "Hoy toca $type del coche"
                                        )
                                    }
                                    createdId != null
                                }

                                isBusy = false
                                if (ok) onDone() else snackbar.showSnackbar("No se pudo guardar")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (isEdit) "Guardar cambios" else "Añadir mantenimiento") }

                    if (isEdit) {
                        OutlinedButton(
                            enabled = !isBusy,
                            onClick = { confirmDelete = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Eliminar") }
                    }
                }
            }

            // Overlay de carga
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

    // Confirmación de borrado
    if (confirmDelete && maintId != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar mantenimiento") },
            text  = { Text("¿Seguro que quieres eliminar este mantenimiento?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    isBusy = true
                    val id = maintId
                    scope.launch {
                        val ok = vm.delete(carId, id)
                        if (ok) ReminderScheduler.cancel(context, carId, id)
                        isBusy = false
                        if (ok) onDone() else snackbar.showSnackbar("No se pudo eliminar")
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

// Ajusta a las 09:00 locales para que la notificación no llegue de madrugada
private fun dateMillisAtNine(dayMillis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
    cal.set(Calendar.HOUR_OF_DAY, 9)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

