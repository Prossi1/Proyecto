package com.example.proyectodieta.data.models

data class ShoppingListItem(
    val id: String = "",
    val ingredientName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val checked: Boolean = false,
    // Referencias a las comidas que usan este ingrediente
    val mealReferences: List<MealReference> = emptyList()
)
