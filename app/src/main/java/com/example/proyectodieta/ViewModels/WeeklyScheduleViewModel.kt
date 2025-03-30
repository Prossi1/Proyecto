package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectodieta.data.models.Meal
import com.example.proyectodieta.data.models.MealPlan
import com.example.proyectodieta.data.models.ScheduledMeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class WeeklyScheduleViewModel : ViewModel() {

    private val _mealPlans = MutableLiveData<List<MealPlan>>(emptyList())
    val mealPlans: LiveData<List<MealPlan>> = _mealPlans

    private val _availableMeals = MutableLiveData<List<Meal>>(emptyList())
    val availableMeals: LiveData<List<Meal>> = _availableMeals

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Cargar el horario semanal desde Firestore
    fun loadWeeklySchedule() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        firestore.collection("users").document(userId)
            .collection("mealPlans")
            .get()
            .addOnSuccessListener { result ->
                val weeklyMealPlans = mutableListOf<MealPlan>()

                for (document in result) {
                    try {
                        // Obtener información básica del plan
                        val dayOfWeek = document.getString("dayOfWeek") ?: ""
                        val planId = document.id

                        // Cargar las comidas programadas para este día
                        loadScheduledMeals(userId, planId, dayOfWeek) { dayPlan ->
                            weeklyMealPlans.add(dayPlan)

                            // Cuando se han cargado todos los días, actualizamos el LiveData
                            if (weeklyMealPlans.size == result.size()) {
                                // Ordenar los planes según el día de la semana
                                val orderedDays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                                weeklyMealPlans.sortBy { orderedDays.indexOf(it.dayOfWeek) }

                                _mealPlans.value = weeklyMealPlans
                                _isLoading.value = false
                            }
                        }
                    } catch (e: Exception) {
                        _error.value = "Error al procesar datos: ${e.message}"
                    }
                }

                // Si no hay planes de comida, crear planes vacíos para cada día
                if (result.isEmpty) {
                    createEmptyWeeklyPlans(userId)
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al cargar la agenda: ${exception.message}"
                _isLoading.value = false
            }
    }

    // Cargar las comidas programadas para un día específico
    private fun loadScheduledMeals(userId: String, planId: String, dayOfWeek: String, onComplete: (MealPlan) -> Unit) {
        firestore.collection("users").document(userId)
            .collection("mealPlans").document(planId)
            .collection("scheduledMeals")
            .get()
            .addOnSuccessListener { mealsSnapshot ->
                val scheduledMeals = mealsSnapshot.documents.mapNotNull { doc ->
                    try {
                        ScheduledMeal(
                            id = doc.id,
                            mealId = doc.getString("mealId") ?: "",
                            mealName = doc.getString("mealName") ?: "",
                            servings = doc.getLong("servings")?.toInt() ?: 1,
                            timeOfDay = doc.getString("timeOfDay") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                onComplete(
                    MealPlan(
                        id = planId,
                        userId = userId,
                        dayOfWeek = dayOfWeek,
                        meals = scheduledMeals
                    )
                )
            }
            .addOnFailureListener {
                // Si falla, devolver un plan sin comidas
                onComplete(
                    MealPlan(
                        id = planId,
                        userId = userId,
                        dayOfWeek = dayOfWeek,
                        meals = emptyList()
                    )
                )
            }
    }

    // Crear planes vacíos para cada día de la semana
    private fun createEmptyWeeklyPlans(userId: String) {
        val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val emptyPlans = mutableListOf<MealPlan>()
        val batch = firestore.batch()

        daysOfWeek.forEach { day ->
            val planId = UUID.randomUUID().toString()
            val planRef = firestore.collection("users").document(userId)
                .collection("mealPlans").document(planId)

            batch.set(planRef, mapOf(
                "dayOfWeek" to day,
                "userId" to userId
            ))

            emptyPlans.add(
                MealPlan(
                    id = planId,
                    userId = userId,
                    dayOfWeek = day,
                    meals = emptyList()
                )
            )
        }

        batch.commit()
            .addOnSuccessListener {
                _mealPlans.value = emptyPlans
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error al crear agenda vacía: ${e.message}"
                _isLoading.value = false
            }
    }

    // Cargar comidas disponibles para añadir a la agenda
    fun loadAvailableMeals() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        firestore.collection("users").document(userId)
            .collection("meals")
            .get()
            .addOnSuccessListener { result ->
                val meals = result.documents.mapNotNull { doc ->
                    try {
                        Meal(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            calories = doc.getLong("calories")?.toInt() ?: 0,
                            carbs = doc.getLong("carbs")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _availableMeals.value = meals
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al cargar comidas: ${exception.message}"
            }
    }

    // Añadir una comida a la agenda
    fun addScheduledMeal(dayOfWeek: String, mealId: String, mealName: String, timeOfDay: String, servings: Int) {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Buscar el plan para el día seleccionado
        val dayPlan = _mealPlans.value?.find { it.dayOfWeek == dayOfWeek }
        if (dayPlan == null) {
            _error.value = "No se encontró el plan para $dayOfWeek"
            _isLoading.value = false
            return
        }

        // Crear el objeto de comida programada
        val scheduledMealId = UUID.randomUUID().toString()
        val scheduledMeal = ScheduledMeal(
            id = scheduledMealId,
            mealId = mealId,
            mealName = mealName,
            servings = servings,
            timeOfDay = timeOfDay
        )

        // Guardar en Firestore
        firestore.collection("users").document(userId)
            .collection("mealPlans").document(dayPlan.id)
            .collection("scheduledMeals").document(scheduledMealId)
            .set(mapOf(
                "mealId" to mealId,
                "mealName" to mealName,
                "servings" to servings,
                "timeOfDay" to timeOfDay
            ))
            .addOnSuccessListener {
                // Recargar los planes después de añadir
                loadWeeklySchedule()
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al guardar comida: ${exception.message}"
                _isLoading.value = false
            }
    }

    // Eliminar una comida programada
    fun removeScheduledMeal(dayOfWeek: String, mealId: String) {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Buscar el plan para el día seleccionado
        val dayPlan = _mealPlans.value?.find { it.dayOfWeek == dayOfWeek }
        if (dayPlan == null) {
            _error.value = "No se encontró el plan para $dayOfWeek"
            _isLoading.value = false
            return
        }

        // Eliminar de Firestore
        firestore.collection("users").document(userId)
            .collection("mealPlans").document(dayPlan.id)
            .collection("scheduledMeals").document(mealId)
            .delete()
            .addOnSuccessListener {
                // Recargar los planes después de eliminar
                loadWeeklySchedule()
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al eliminar comida: ${exception.message}"
                _isLoading.value = false
            }
    }
}