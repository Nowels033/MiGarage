package com.example.migarage.model

data class Car(
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val currentKm: Int = 0,
    val imageUrl: String? = null

)
