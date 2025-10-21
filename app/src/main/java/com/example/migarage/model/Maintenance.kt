package com.example.migarage.model

data class Maintenance(
    val id: String = "",
    val type: String = "",       // p.ej. Aceite, Filtros, Frenos…
    val dateMillis: Long = 0L,
    val km: Int = 0,
    val cost: Double = 0.0,
    val notes: String = ""
)
