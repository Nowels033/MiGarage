package com.example.migarage.ui.addcar

import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Car

class AddCarViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun saveCar(brand: String, model: String, plate: String, km: Int): Boolean {
        return try {
            repo.addCar(
                Car(brand = brand, model = model, plate = plate, currentKm = km)
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}
