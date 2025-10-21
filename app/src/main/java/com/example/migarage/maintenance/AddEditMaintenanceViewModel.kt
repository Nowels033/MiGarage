package com.example.migarage.ui.maintenance

import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Maintenance

class AddEditMaintenanceViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun load(carId: String, maintId: String) = repo.getMaintenance(carId, maintId)

    suspend fun saveNew(
        carId: String,
        type: String,
        dateMillis: Long,
        km: Int,
        cost: Double,
        notes: String
    ): Boolean = try {
        repo.addMaintenance(
            carId,
            Maintenance(type = type, dateMillis = dateMillis, km = km, cost = cost, notes = notes)
        )
        true
    } catch (_: Exception) { false }

    suspend fun saveEdit(
        carId: String,
        maintId: String,
        type: String,
        dateMillis: Long,
        km: Int,
        cost: Double,
        notes: String
    ): Boolean = try {
        repo.updateMaintenance(
            carId,
            Maintenance(id = maintId, type = type, dateMillis = dateMillis, km = km, cost = cost, notes = notes)
        )
        true
    } catch (_: Exception) { false }
}
