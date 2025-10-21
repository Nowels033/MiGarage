package com.example.migarage.ui.maintenance

import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Maintenance

class AddEditMaintenanceViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun load(carId: String, maintId: String) = repo.getMaintenance(carId, maintId)

    // NUEVO: devuelve el id creado o null
    suspend fun saveNewReturnId(
        carId: String,
        type: String,
        dateMillis: Long,
        km: Int,
        cost: Double,
        notes: String
    ): String? = try {
        repo.addMaintenanceReturnId(
            carId,
            Maintenance(type = type, dateMillis = dateMillis, km = km, cost = cost, notes = notes)
        )
        // el repo devuelve el id en un campo internal, así que mejor devuelve String
    } catch (_: Exception) { null }

    suspend fun saveNew(
        carId: String, type: String, dateMillis: Long, km: Int, cost: Double, notes: String
    ): Boolean = saveNewReturnId(carId, type, dateMillis, km, cost, notes) != null

    suspend fun saveEdit(
        carId: String, maintId: String, type: String, dateMillis: Long, km: Int, cost: Double, notes: String
    ): Boolean = try {
        repo.updateMaintenance(
            carId,
            Maintenance(id = maintId, type = type, dateMillis = dateMillis, km = km, cost = cost, notes = notes)
        )
        true
    } catch (_: Exception) { false }

    suspend fun delete(carId: String, maintId: String): Boolean = try {
        repo.deleteMaintenance(carId, maintId)
        true
    } catch (_: Exception) { false }
}
