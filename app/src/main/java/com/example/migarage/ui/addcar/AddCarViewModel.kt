package com.example.migarage.ui.addcar

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository

class AddCarViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun saveCar(
        brand: String,
        model: String,
        plate: String,
        km: Int,
        imageUri: Uri?
    ): Boolean = try {
        repo.addCarWithOptionalImage(brand, model, plate, km, imageUri)
        true
    } catch (_: Exception) { false }
}
