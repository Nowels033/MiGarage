package com.example.migarage.ui.cardetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.migarage.R
import com.example.migarage.model.Car

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    vm: CarDetailViewModel = viewModel()
) {
    val snackbar = remember { SnackbarHostState() }
    var car by remember { mutableStateOf<Car?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(carId) {
        loading = true
        car = vm.load(carId)
        loading = false
        if (car == null) snackbar.showSnackbar("Coche no encontrado")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del coche") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = car != null) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar Coche")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (car != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Editar") },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = onEdit
                )
            }
        }
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp)
        ) {
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                car == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontró el coche")
                    }
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Foto
                        AsyncImage(
                            model = car!!.imageUrl,
                            contentDescription = "Foto del coche",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = painterResource(R.drawable.ic_car_placeholder),
                            error = painterResource(R.drawable.ic_car_placeholder),
                            fallback = painterResource(R.drawable.ic_car_placeholder)
                        )

                        // Propiedades
                        DetailRow(label = "Marca", value = car!!.brand)
                        DetailRow(label = "Modelo", value = car!!.model)
                        DetailRow(label = "Matrícula", value = car!!.plate)
                        DetailRow(label = "Kilómetros", value = "%,d".format(car!!.currentKm))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}
