package com.example.migarage.ui.maintenance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.migarage.model.Maintenance
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListScreen(
    carId: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    vm: MaintenanceListViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(carId) { vm.start(carId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mantenimientos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin mantenimientos. Pulsa +")
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.items) { m ->
                            MaintRow(m, onClick = { onEdit(m.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintRow(m: Maintenance, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(m.type, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "${fmt.format(Date(m.dateMillis))}  •  ${m.km} km  •  ${"%.2f".format(m.cost)} €",
                style = MaterialTheme.typography.bodyMedium
            )
            if (m.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(m.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
