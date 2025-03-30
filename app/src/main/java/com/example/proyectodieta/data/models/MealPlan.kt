package com.example.proyectodieta.data.models


data class MealPlan(
    val id: String = "",
    val userId: String = "",
    val dayOfWeek: String = "", // "Lunes", "Martes", etc.
    val meals: List<ScheduledMeal> = emptyList()
)