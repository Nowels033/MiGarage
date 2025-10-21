package com.example.migarage.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Maintenance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintListState(
    val items: List<Maintenance> = emptyList(),
    val loading: Boolean = true
)

class MaintenanceListViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MaintListState())
    val state: StateFlow<MaintListState> = _state

    fun start(carId: String) {
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            repo.listenMaintenances(carId).collect { list ->
                _state.update { it.copy(items = list, loading = false) }
            }
        }
    }

    suspend fun delete(carId: String, maintId: String): Boolean = try {
        repo.deleteMaintenance(carId, maintId)
        true
    } catch (_: Exception) { false }
}
