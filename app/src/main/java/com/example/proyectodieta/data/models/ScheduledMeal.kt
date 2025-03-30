package com.example.proyectodieta.data.models

data class ScheduledMeal(
    val id: String = "",
    val mealId: String = "",
    val mealName: String = "",
    val servings: Int = 1,
    val timeOfDay: String = "" // "Desayuno", "Almuerzo", "Cena", etc.
)