package com.example.migarage.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Car
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val displayName: String = "",
    val photoUrl: String? = null,
    val cars: List<Car> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val repo: CarRepository = CarRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        // Datos del usuario actual
        val user = auth.currentUser
        _state.update {
            it.copy(
                displayName = user?.displayName.orEmpty(),
                photoUrl = user?.photoUrl?.toString()
            )
        }

        // Suscripción en tiempo real a los coches del usuario
        viewModelScope.launch {
            repo.listenCars().collect { list ->
                _state.update { st -> st.copy(cars = list, loading = false) }
            }
        }
    }

    // Opcional: para pruebas rápidas
    fun addDemoCar() = viewModelScope.launch {
        repo.addCar(Car(brand = "Toyota", model = "Corolla", plate = "1234-ABC", currentKm = 75210))
    }
}
