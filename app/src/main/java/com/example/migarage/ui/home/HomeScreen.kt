package com.example.migarage.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.migarage.model.Car
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.migarage.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddCar: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onCarClick: ((String) -> Unit)? = null,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val gso = remember { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build() }
    val gsc = remember { GoogleSignIn.getClient(context, gso) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MiGarage") },
                actions = {
                    IconButton(onClick = {
                        // Cerrar sesión Firebase y Google
                        auth.signOut()
                        gsc.signOut().addOnCompleteListener {
                            onLogout?.invoke() // Vuelve al login
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddCar?.invoke() }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir coche")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // === CABECERA DE USUARIO ===
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(state.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        state.displayName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Tus coches", style = MaterialTheme.typography.bodySmall)
                }
            }

            // === LISTA DE COCHES ===
            when {
                state.loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                state.cars.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text("Aún no tienes coches. Pulsa + para añadir 🚗") }
                }

                else -> {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.cars) { car ->
                            CarCard(
                                car = car,
                                onClick = { onCarClick?.invoke(car.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// === COMPONENTE PARA CADA COCHE ===
@Composable
private fun CarCard(car: Car, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = "Foto coche",
                modifier = Modifier.size(64.dp),
                placeholder = painterResource(R.drawable.ic_car_placeholder),
                error = painterResource(R.drawable.ic_car_placeholder),
                fallback = painterResource(R.drawable.ic_car_placeholder)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("${car.brand} ${car.model}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Matrícula: ${car.plate}", style = MaterialTheme.typography.bodyMedium)
                Text("Km: ${car.currentKm}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

