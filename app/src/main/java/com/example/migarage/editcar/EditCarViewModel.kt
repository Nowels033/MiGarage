package com.example.migarage.ui.editcar

import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Car

class EditCarViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun load(carId: String) = repo.getCar(carId)


    suspend fun save(
        carId: String,
        brand: String,
        model: String,
        plate: String,
        km: Int
    ): Boolean = try {
        require(carId.isNotBlank())
        repo.updateCar(Car(id = carId, brand = brand, model = model, plate = plate, currentKm = km))
        true
    } catch (_: Exception) { false }

    suspend fun delete(carId: String): Boolean = try {
        repo.deleteCar(carId)
        true
    } catch (_: Exception) { false }
}

