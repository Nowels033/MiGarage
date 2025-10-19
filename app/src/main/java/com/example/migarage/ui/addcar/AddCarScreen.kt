package com.example.migarage.ui.addcar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    onCarSaved: () -> Unit,
    vm: AddCarViewModel = viewModel()
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Añadir coche") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = plate,
                onValueChange = { plate = it.uppercase() },
                label = { Text("Matrícula (ej. 1234-ABC)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = km,
                onValueChange = { if (it.all(Char::isDigit)) km = it },
                label = { Text("Kilómetros actuales") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val error = when {
                        brand.isBlank() -> "La marca es obligatoria"
                        model.isBlank() -> "El modelo es obligatorio"
                        plate.length !in 6..10 -> "Matrícula inválida"
                        (km.toIntOrNull() ?: -1) < 0 -> "Kilómetros inválidos"
                        else -> null
                    }
                    if (error != null) {
                        scope.launch { snackbarHostState.showSnackbar(error) }
                        return@Button
                    }

                    scope.launch {
                        val ok = vm.saveCar(
                            brand.trim(),
                            model.trim(),
                            plate.trim(),
                            (km.toIntOrNull() ?: 0)
                        )
                        if (ok) {
                            snackbarHostState.showSnackbar("Coche guardado 🚗")
                            onCarSaved()
                        } else {
                            snackbarHostState.showSnackbar("Error al guardar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar coche")
            }
        }
    }
}
