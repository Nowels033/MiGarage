package com.example.migarage.ui.addcar

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
fun AddCarScreen(
    onCarSaved: () -> Unit,
    vm: AddCarViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }

    // Opción B: bloquea UI mientras guarda y navega al terminar
    var isBusy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Añadir coche") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Matrícula (ej. 1234-ABC)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = km,
                    onValueChange = { if (it.all(Char::isDigit)) km = it },
                    label = { Text("Kilómetros actuales") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isBusy
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    enabled = !isBusy,
                    onClick = {
                        // Validaciones rápidas
                        val error = when {
                            brand.isBlank() -> "La marca es obligatoria"
                            model.isBlank() -> "El modelo es obligatorio"
                            plate.isBlank() -> "La matrícula es obligatoria"
                            (km.toIntOrNull() ?: -1) < 0 -> "Kilómetros inválidos"
                            else -> null
                        }
                        if (error != null) {
                            scope.launch { snackbar.showSnackbar(error) }
                            return@Button
                        }

                        isBusy = true
                        scope.launch {
                            val ok = vm.saveCar(
                                brand.trim(),
                                model.trim(),
                                plate.trim(),
                                km.toIntOrNull() ?: 0
                            )
                            isBusy = false
                            if (ok) onCarSaved()
                            else snackbar.showSnackbar("No se pudo guardar el coche")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar coche")
                }
            }

            // Overlay de carga
            if (isBusy) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(12.dp))
                            Text("Guardando…")
                        }
                    }
                }
            }
        }
    }
}

