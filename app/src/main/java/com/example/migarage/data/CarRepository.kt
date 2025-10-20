package com.example.migarage.data

import android.net.Uri
import com.example.migarage.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CarRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private fun carsRef(uid: String) = db.collection("users").document(uid).collection("cars")
    private fun carImageRef(uid: String, carId: String) =
        storage.reference.child("users/$uid/cars/$carId.jpg")

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
                        currentKm = (d.getLong("currentKm") ?: 0L).toInt(),
                        imageUrl = d.getString("imageUrl")
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /** Crear coche con imagen opcional. Si [imageUri] es null, almacena el coche sin imageUrl. */
    suspend fun addCarWithOptionalImage(
        brand: String,
        model: String,
        plate: String,
        km: Int,
        imageUri: Uri?
    ) {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }

        // 1) Reservar ID de documento para usarlo como nombre del archivo
        val newDoc = carsRef(uid).document()
        var downloadUrl: String? = null

        // 2) Si hay imagen, subirla a Storage y obtener downloadUrl
        if (imageUri != null) {
            val ref = carImageRef(uid, newDoc.id)
            ref.putFile(imageUri).await()
            downloadUrl = ref.downloadUrl.await().toString()
        }

        // 3) Guardar el documento en Firestore
        val data = mapOf(
            "brand" to brand,
            "model" to model,
            "plate" to plate,
            "currentKm" to km,
            "imageUrl" to downloadUrl // puede ser null si no se subió imagen
        )
        newDoc.set(data).await()
    }

    // ------- ya existentes para CRUD -------
    suspend fun getCar(carId: String): Car? {
        val uid = requireNotNull(auth.currentUser?.uid)
        val doc = carsRef(uid).document(carId).get().await()
        return if (doc.exists()) {
            Car(
                id = doc.id,
                brand = doc.getString("brand") ?: "",
                model = doc.getString("model") ?: "",
                plate = doc.getString("plate") ?: "",
                currentKm = (doc.getLong("currentKm") ?: 0L).toInt(),
                imageUrl = doc.getString("imageUrl")
            )
        } else null
    }

    suspend fun updateCar(car: Car) {
        val uid = requireNotNull(auth.currentUser?.uid)
        require(car.id.isNotBlank())
        val data = mapOf(
            "brand" to car.brand,
            "model" to car.model,
            "plate" to car.plate,
            "currentKm" to car.currentKm,
            "imageUrl" to car.imageUrl
        )
        carsRef(uid).document(car.id).set(data).await()
    }

    suspend fun deleteCar(carId: String) {
        val uid = requireNotNull(auth.currentUser?.uid)
        // borra Firestore
        carsRef(uid).document(carId).delete().await()
        // intenta borrar imagen asociada (si existiera)
        runCatching { carImageRef(uid, carId).delete().await() }
    }
}
