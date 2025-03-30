package com.example.proyectodieta.data.models

data class UserProfile (
    val email: String = "",
    val name: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val age: Int = 0,
    val gender: String = "",
    val fitnessGoal: String = ""
)