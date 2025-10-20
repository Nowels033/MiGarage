package com.example.migarage.data

import com.example.migarage.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CarRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun carsRef(uid: String) = db.collection("users").document(uid).collection("cars")

    fun listenCars(): Flow<List<Car>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }

        val reg = carsRef(uid).orderBy("brand", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { d ->
                    Car(
                        id = d.id,
                        brand = d.getString("brand") ?: "",
                        model = d.getString("model") ?: "",
                        plate = d.getString("plate") ?: "",
                        currentKm = (d.getLong("currentKm") ?: 0L).toInt()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addCar(car: Car) {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }
        val data = mapOf(
            "brand" to car.brand, "model" to car.model,
            "plate" to car.plate, "currentKm" to car.currentKm
        )
        carsRef(uid).add(data).await()
    }

    suspend fun getCar(carId: String): Car? {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }
        val doc = carsRef(uid).document(carId).get().await()
        return if (doc.exists()) {
            Car(
                id = doc.id,
                brand = doc.getString("brand") ?: "",
                model = doc.getString("model") ?: "",
                plate = doc.getString("plate") ?: "",
                currentKm = (doc.getLong("currentKm") ?: 0L).toInt()
            )
        } else null
    }

    suspend fun updateCar(car: Car) {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }
        require(car.id.isNotBlank()) { "Car ID vacío" }
        val data = mapOf(
            "brand" to car.brand, "model" to car.model,
            "plate" to car.plate, "currentKm" to car.currentKm
        )
        carsRef(uid).document(car.id).set(data).await()
    }

    suspend fun deleteCar(carId: String) {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }
        carsRef(uid).document(carId).delete().await()
    }
}

