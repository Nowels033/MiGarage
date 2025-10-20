package com.example.migarage.ui.cardetail

import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Car

class CarDetailViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun load(carId: String): Car? = repo.getCar(carId)
}
