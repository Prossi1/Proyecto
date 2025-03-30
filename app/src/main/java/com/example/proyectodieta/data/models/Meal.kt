package com.example.proyectodieta.data.models


data class Meal(
    val id: String = "",
    val name: String = "",
    val calories: Int = 0,
    val carbs: Int = 0,
    val ingredients: List<Ingredient> = emptyList()
)
