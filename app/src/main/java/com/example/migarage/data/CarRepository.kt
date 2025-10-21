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
import kotlinx.coroutines.channels.awaitClose




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

    /** Actualiza datos del coche. Si [newImage] no es null, sube imagen y actualiza imageUrl. */
    suspend fun updateCarWithOptionalImage(car: Car, newImage: Uri?) {
        val uid = requireNotNull(auth.currentUser?.uid) { "Usuario no autenticado" }
        require(car.id.isNotBlank()) { "Car ID vacío" }

        var finalUrl = car.imageUrl

        if (newImage != null) {
            // mismo path que usamos al crear: users/<uid>/cars/<carId>.jpg
            val ref = FirebaseStorage.getInstance()
                .reference.child("users/$uid/cars/${car.id}.jpg")
            ref.putFile(newImage).await()
            finalUrl = ref.downloadUrl.await().toString()
        }

        val data = mapOf(
            "brand" to car.brand,
            "model" to car.model,
            "plate" to car.plate,
            "currentKm" to car.currentKm,
            "imageUrl" to finalUrl
        )
        carsRef(uid).document(car.id).set(data).await()
    }
    // --- MANTENIMIENTOS (subcolección) ---
// Ref: users/{uid}/cars/{carId}/maintenances
    private fun maintRef(uid: String, carId: String) =
        db.collection("users").document(uid).collection("cars")
            .document(carId).collection("maintenances")

    fun listenMaintenances(carId: String): kotlinx.coroutines.flow.Flow<List<com.example.migarage.model.Maintenance>> =
        kotlinx.coroutines.flow.callbackFlow {
            val uid = auth.currentUser?.uid
            if (uid == null) { trySend(emptyList()); close(); return@callbackFlow }

            val reg = maintRef(uid, carId)
                .orderBy("dateMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                    val list = snap?.documents?.map { d ->
                        com.example.migarage.model.Maintenance(
                            id = d.id,
                            type = d.getString("type") ?: "",
                            dateMillis = d.getLong("dateMillis") ?: 0L,
                            km = (d.getLong("km") ?: 0L).toInt(),
                            cost = d.getDouble("cost") ?: 0.0,
                            notes = d.getString("notes") ?: ""
                        )
                    } ?: emptyList()
                    trySend(list)
                }
            awaitClose { reg.remove() }

        }

    suspend fun addMaintenance(
        carId: String,
        m: com.example.migarage.model.Maintenance
    ) {
        val uid = requireNotNull(auth.currentUser?.uid)
        val data = mapOf(
            "type" to m.type,
            "dateMillis" to m.dateMillis,
            "km" to m.km,
            "cost" to m.cost,
            "notes" to m.notes
        )
        maintRef(uid, carId).add(data).await()
    }

    suspend fun getMaintenance(carId: String, maintId: String): com.example.migarage.model.Maintenance? {
        val uid = requireNotNull(auth.currentUser?.uid)
        val doc = maintRef(uid, carId).document(maintId).get().await()
        return if (doc.exists()) com.example.migarage.model.Maintenance(
            id = doc.id,
            type = doc.getString("type") ?: "",
            dateMillis = doc.getLong("dateMillis") ?: 0L,
            km = (doc.getLong("km") ?: 0L).toInt(),
            cost = doc.getDouble("cost") ?: 0.0,
            notes = doc.getString("notes") ?: ""
        ) else null
    }

    suspend fun updateMaintenance(
        carId: String,
        m: com.example.migarage.model.Maintenance
    ) {
        val uid = requireNotNull(auth.currentUser?.uid)
        require(m.id.isNotBlank())
        val data = mapOf(
            "type" to m.type,
            "dateMillis" to m.dateMillis,
            "km" to m.km,
            "cost" to m.cost,
            "notes" to m.notes
        )
        maintRef(uid, carId).document(m.id).set(data).await()
    }

    suspend fun deleteMaintenance(carId: String, maintId: String) {
        val uid = requireNotNull(auth.currentUser?.uid)
        maintRef(uid, carId).document(maintId).delete().await()
    }


}
