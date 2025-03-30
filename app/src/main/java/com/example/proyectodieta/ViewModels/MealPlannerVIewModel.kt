package com.example.proyectodieta.ViewModels

import com.example.proyectodieta.data.models.Ingredient
import com.example.proyectodieta.data.models.Meal
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class MealPlannerViewModel : ViewModel() {

    private val _currentMeal = MutableLiveData<Meal>(Meal())
    val currentMeal: LiveData<Meal> = _currentMeal

    private val _ingredients = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Actualizar nombre de la comida
    fun updateMealName(name: String) {
        _currentMeal.value = _currentMeal.value?.copy(name = name)
    }

    // Actualizar calorías
    fun updateCalories(calories: Int) {
        _currentMeal.value = _currentMeal.value?.copy(calories = calories)
    }

    // Actualizar carbohidratos
    fun updateCarbs(carbs: Int) {
        _currentMeal.value = _currentMeal.value?.copy(carbs = carbs)
    }

    // Añadir ingrediente
    fun addIngredient() {
        val newIngredient = Ingredient(id = UUID.randomUUID().toString())
        val currentList = _ingredients.value ?: emptyList()
        _ingredients.value = currentList + newIngredient
    }

    // Eliminar ingrediente
    fun removeIngredient(id: String) {
        _ingredients.value = _ingredients.value?.filter { it.id != id }
    }

    // Actualizar ingrediente
    fun updateIngredient(updatedIngredient: Ingredient) {
        val currentList = _ingredients.value ?: return
        val updatedList = currentList.map {
            if (it.id == updatedIngredient.id) updatedIngredient else it
        }
        _ingredients.value = updatedList
    }

    // Guardar comida en Firestore
    fun saveMeal() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        val currentMeal = _currentMeal.value ?: Meal()
        val mealWithIngredients = currentMeal.copy(
            id = currentMeal.id.ifEmpty { UUID.randomUUID().toString() },
            ingredients = _ingredients.value ?: emptyList()
        )

        val mealMap = hashMapOf(
            "id" to mealWithIngredients.id,
            "name" to mealWithIngredients.name,
            "calories" to mealWithIngredients.calories,
            "carbs" to mealWithIngredients.carbs
        )

        // Guardar la comida
        firestore.collection("users").document(userId)
            .collection("meals").document(mealWithIngredients.id)
            .set(mealMap)
            .addOnSuccessListener {
                // Guardar los ingredientes como subcolección
                val batch = firestore.batch()

                // Eliminar ingredientes existentes primero
                firestore.collection("users").document(userId)
                    .collection("meals").document(mealWithIngredients.id)
                    .collection("ingredients")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            batch.delete(doc.reference)
                        }

                        // Añadir nuevos ingredientes
                        mealWithIngredients.ingredients.forEach { ingredient ->
                            val ingredientRef = firestore.collection("users").document(userId)
                                .collection("meals").document(mealWithIngredients.id)
                                .collection("ingredients").document(ingredient.id)

                            batch.set(ingredientRef, ingredient)
                        }

                        // Commit el batch
                        batch.commit()
                            .addOnSuccessListener {
                                _isLoading.value = false
                                // Reset para una nueva comida
                                _currentMeal.value = Meal()
                                _ingredients.value = emptyList()
                            }
                            .addOnFailureListener { e ->
                                _error.value = "Error al guardar los ingredientes: ${e.message}"
                                _isLoading.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error al actualizar ingredientes: ${e.message}"
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al guardar la comida: ${e.message}"
                _isLoading.value = false
            }
    }

    // Limpiar formulario
    fun clearForm() {
        _currentMeal.value = Meal()
        _ingredients.value = emptyList()
        _error.value = null
    }
}