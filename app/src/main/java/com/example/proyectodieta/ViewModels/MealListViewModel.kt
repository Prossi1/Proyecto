package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectodieta.data.models.Ingredient
import com.example.proyectodieta.data.models.Meal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MealListViewModel : ViewModel() {

    private val _meals = MutableLiveData<List<Meal>>(emptyList())
    val meals: LiveData<List<Meal>> = _meals

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Cargar la lista de comidas del usuario desde Firestore
    fun loadMeals() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        firestore.collection("users").document(userId)
            .collection("meals")
            .get()
            .addOnSuccessListener { result ->
                val mealsList = mutableListOf<Meal>()

                // Procesar cada documento de comida
                for (document in result) {
                    val meal = Meal(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        calories = document.getLong("calories")?.toInt() ?: 0,
                        carbs = document.getLong("carbs")?.toInt() ?: 0
                    )

                    // Cargar los ingredientes para esta comida
                    loadIngredientsForMeal(userId, meal) { completedMeal ->
                        mealsList.add(completedMeal)

                        // Si es la última comida, actualizar la lista
                        if (mealsList.size == result.size()) {
                            _meals.value = mealsList
                            _isLoading.value = false
                        }
                    }
                }

                // Si no hay comidas, actualizar el estado
                if (result.isEmpty) {
                    _meals.value = emptyList()
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al cargar las comidas: ${exception.message}"
                _isLoading.value = false
            }
    }

    // Cargar los ingredientes para una comida específica
    private fun loadIngredientsForMeal(userId: String, meal: Meal, onComplete: (Meal) -> Unit) {
        firestore.collection("users").document(userId)
            .collection("meals").document(meal.id)
            .collection("ingredients")
            .get()
            .addOnSuccessListener { ingredientsSnapshot ->
                val ingredients = ingredientsSnapshot.mapNotNull { doc ->
                    try {
                        Ingredient(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            quantity = doc.getString("quantity") ?: "",
                            unit = doc.getString("unit") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onComplete(meal.copy(ingredients = ingredients))
            }
            .addOnFailureListener {
                // Si falla, devolver la comida sin ingredientes
                onComplete(meal)
            }
    }

    // Eliminar una comida
    fun deleteMeal(mealId: String) {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Primero eliminar los ingredientes
        firestore.collection("users").document(userId)
            .collection("meals").document(mealId)
            .collection("ingredients")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()

                // Eliminar cada documento de ingrediente
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Luego eliminar la comida
                batch.delete(
                    firestore.collection("users").document(userId)
                        .collection("meals").document(mealId)
                )

                // Ejecutar el batch
                batch.commit()
                    .addOnSuccessListener {
                        // Recargar la lista después de eliminar
                        loadMeals()
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error al eliminar la comida: ${e.message}"
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar ingredientes: ${e.message}"
                _isLoading.value = false
            }
    }
}