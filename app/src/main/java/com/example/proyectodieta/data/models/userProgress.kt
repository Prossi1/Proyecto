package com.example.proyectodieta.data.models

import java.util.Date

data class UserProgress(
    val id: String = "",
    val userId: String = "",
    val date: Date = Date(),
    val weight: Double = 0.0,
    val notes: String = "",
    val measurements: Map<String, Double> = mapOf()  // Para medidas como cintura, brazos, etc.
)