package com.example.migarage.ui.editcar

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.migarage.data.CarRepository
import com.example.migarage.model.Car

class EditCarViewModel(
    private val repo: CarRepository = CarRepository()
) : ViewModel() {

    suspend fun load(carId: String) = repo.getCar(carId)

    // Guardar cambios + subir nueva imagen si corresponde
    suspend fun save(
        carId: String,
        brand: String,
        model: String,
        plate: String,
        km: Int,
        newImage: Uri?,               // imagen nueva (puede ser null)
        currentImageUrl: String?      // url actual (puede ser null)
    ): Boolean = try {
        require(carId.isNotBlank())
        val base = Car(
            id = carId,
            brand = brand,
            model = model,
            plate = plate,
            currentKm = km,
            imageUrl = currentImageUrl
        )
        repo.updateCarWithOptionalImage(base, newImage)
        true
    } catch (_: Exception) { false }

    suspend fun delete(carId: String): Boolean = try {
        repo.deleteCar(carId)
        true
    } catch (_: Exception) { false }
}
