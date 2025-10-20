package com.example.migarage.ui.editcar

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    carId: String,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    vm: EditCarViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Estado del formulario
    var brand: String by remember { mutableStateOf("") }
    var model: String by remember { mutableStateOf("") }
    var plate: String by remember { mutableStateOf("") }
    var km: String by remember { mutableStateOf("") }

    // Estado de carga (Opción B)
    var isBusy by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // Cargar datos
    LaunchedEffect(carId) {
        isBusy = true
        val car = vm.load(carId)
        if (car != null) {
            brand = car.brand
            model = car.model
            plate = car.plate
            km = car.currentKm.toString()
        } else {
            snackbar.showSnackbar("Coche no encontrado")
        }
        isBusy = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar coche") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                OutlinedTextField(
                    value = brand, onValueChange = { brand = it },
                    label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = model, onValueChange = { model = it },
                    label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = plate, onValueChange = { plate = it.uppercase() },
                    label = { Text("Matrícula") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isBusy
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

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        enabled = !isBusy,
                        onClick = {
                            // Validación simple
                            val err = when {
                                brand.isBlank() -> "La marca es obligatoria"
                                model.isBlank() -> "El modelo es obligatorio"
                                plate.isBlank() -> "La matrícula es obligatoria"
                                (km.toIntOrNull() ?: -1) < 0 -> "Kilómetros inválidos"
                                else -> null
                            }
                            if (err != null) {
                                scope.launch { snackbar.showSnackbar(err) }
                                return@Button
                            }

                            isBusy = true
                            scope.launch {
                                val ok = vm.save(
                                    carId = carId,
                                    brand = brand.trim(),
                                    model = model.trim(),
                                    plate = plate.trim(),
                                    km = km.toIntOrNull() ?: 0
                                )
                                isBusy = false
                                if (ok) {
                                    onSaved() // navegar cuando Firestore confirma
                                } else {
                                    snackbar.showSnackbar("Error al guardar")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar cambios") }

                    OutlinedButton(
                        enabled = !isBusy,
                        onClick = { showConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("Eliminar") }
                }
            }

            // Overlay de carga (bloquea la UI con feedback)
            if (isBusy) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(12.dp))
                            Text("Procesando…")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para borrar
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Eliminar coche") },
            text = { Text("¿Seguro que quieres eliminar este coche? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        isBusy = true
                        scope.launch {
                            val ok = vm.delete(carId)
                            isBusy = false
                            if (ok) onDeleted() else snackbar.showSnackbar("No se pudo eliminar")
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } }
        )
    }
}
