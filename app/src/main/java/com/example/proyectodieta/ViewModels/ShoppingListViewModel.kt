package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectodieta.data.models.Ingredient
import com.example.proyectodieta.data.models.MealPlan
import com.example.proyectodieta.data.models.MealReference
import com.example.proyectodieta.data.models.ShoppingListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ShoppingListViewModel : ViewModel() {

    private val _shoppingItems = MutableLiveData<List<ShoppingListItem>>(emptyList())
    val shoppingItems: LiveData<List<ShoppingListItem>> = _shoppingItems

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Cargar la lista de compras actual
    fun loadShoppingList() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        firestore.collection("users").document(userId)
            .collection("shoppingList")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { doc ->
                    try {
                        // Cargar referencias a comidas
                        val mealRefsData = doc.get("mealReferences") as? List<Map<String, Any>> ?: emptyList()
                        val mealRefs = mealRefsData.map { refData ->
                            MealReference(
                                mealId = (refData["mealId"] as? String) ?: "",
                                mealName = (refData["mealName"] as? String) ?: "",
                                dayOfWeek = (refData["dayOfWeek"] as? String) ?: "",
                                servings = (refData["servings"] as? Long)?.toInt() ?: 1
                            )
                        }

                        ShoppingListItem(
                            id = doc.id,
                            ingredientName = doc.getString("ingredientName") ?: "",
                            quantity = doc.getDouble("quantity") ?: 0.0,
                            unit = doc.getString("unit") ?: "",
                            checked = doc.getBoolean("checked") ?: false,
                            mealReferences = mealRefs
                        )
                    } catch (e: Exception) {
                        _error.value = "Error al procesar item: ${e.message}"
                        null
                    }
                }
                _shoppingItems.value = items
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al cargar lista de compras: ${exception.message}"
                _isLoading.value = false
            }
    }

    // Generar lista de compras a partir de la agenda semanal
    fun generateShoppingList() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // 1. Obtener todos los planes de comida de la semana
        firestore.collection("users").document(userId)
            .collection("mealPlans")
            .get()
            .addOnSuccessListener { plansResult ->
                val mealPlans = plansResult.documents.mapNotNull { doc ->
                    val dayOfWeek = doc.getString("dayOfWeek") ?: return@mapNotNull null
                    val planId = doc.id
                    Pair(planId, dayOfWeek)
                }

                if (mealPlans.isEmpty()) {
                    _error.value = "No hay comidas planificadas"
                    _isLoading.value = false
                    return@addOnSuccessListener
                }

                // Mapa para acumular los ingredientes
                val ingredientsMap = mutableMapOf<String, ShoppingListItem>()

                // Contador para seguir el progreso
                var processedPlans = 0

                // 2. Para cada día, obtener las comidas programadas
                mealPlans.forEach { (planId, dayOfWeek) ->
                    firestore.collection("users").document(userId)
                        .collection("mealPlans").document(planId)
                        .collection("scheduledMeals")
                        .get()
                        .addOnSuccessListener { mealsResult ->
                            // Para cada comida programada, obtener sus ingredientes
                            if (mealsResult.isEmpty) {
                                processedPlans++
                                if (processedPlans == mealPlans.size) {
                                    saveShoppingList(ingredientsMap.values.toList())
                                }
                                return@addOnSuccessListener
                            }

                            // Contador para seguir progreso por día
                            var processedMeals = 0

                            mealsResult.documents.forEach { mealDoc ->
                                val scheduledMeal = try {
                                    val mealId = mealDoc.getString("mealId") ?: ""
                                    val mealName = mealDoc.getString("mealName") ?: ""
                                    val servings = mealDoc.getLong("servings")?.toInt() ?: 1
                                    Triple(mealId, mealName, servings)
                                } catch (e: Exception) {
                                    processedMeals++
                                    if (processedMeals == mealsResult.size() &&
                                        processedPlans == mealPlans.size - 1) {
                                        saveShoppingList(ingredientsMap.values.toList())
                                    }
                                    return@forEach
                                }

                                // Obtener los ingredientes de esta comida
                                val (mealId, mealName, servings) = scheduledMeal

                                // 3. Obtener ingredientes de cada comida
                                firestore.collection("users").document(userId)
                                    .collection("meals").document(mealId)
                                    .collection("ingredients")
                                    .get()
                                    .addOnSuccessListener { ingredientsResult ->
                                        ingredientsResult.documents.forEach { ingredientDoc ->
                                            try {
                                                val ingredient = Ingredient(
                                                    id = ingredientDoc.id,
                                                    name = ingredientDoc.getString("name") ?: "",
                                                    quantity = ingredientDoc.getString("quantity") ?: "",
                                                    unit = ingredientDoc.getString("unit") ?: ""
                                                )

                                                // Sólo agregar a la lista si tiene nombre
                                                if (ingredient.name.isNotBlank()) {
                                                    // 4. Acumular ingredientes en el mapa
                                                    val key = "${ingredient.name.toLowerCase()}_${ingredient.unit.toLowerCase()}"

                                                    val mealRef = MealReference(
                                                        mealId = mealId,
                                                        mealName = mealName,
                                                        dayOfWeek = dayOfWeek,
                                                        servings = servings
                                                    )

                                                    // Convertir cantidad a número si es posible
                                                    val quantityNum = ingredient.quantity.toDoubleOrNull() ?: 1.0
                                                    val adjustedQuantity = quantityNum * servings

                                                    if (ingredientsMap.containsKey(key)) {
                                                        // Actualizar item existente
                                                        val currentItem = ingredientsMap[key]!!
                                                        val updatedItem = currentItem.copy(
                                                            quantity = currentItem.quantity + adjustedQuantity,
                                                            mealReferences = currentItem.mealReferences + mealRef
                                                        )
                                                        ingredientsMap[key] = updatedItem
                                                    } else {
                                                        // Crear nuevo item
                                                        val shoppingItem = ShoppingListItem(
                                                            id = UUID.randomUUID().toString(),
                                                            ingredientName = ingredient.name,
                                                            quantity = adjustedQuantity,
                                                            unit = ingredient.unit,
                                                            mealReferences = listOf(mealRef)
                                                        )
                                                        ingredientsMap[key] = shoppingItem
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Ignorar ingredientes con error
                                            }
                                        }

                                        processedMeals++
                                        if (processedMeals == mealsResult.size()) {
                                            processedPlans++
                                            if (processedPlans == mealPlans.size) {
                                                saveShoppingList(ingredientsMap.values.toList())
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        processedMeals++
                                        if (processedMeals == mealsResult.size()) {
                                            processedPlans++
                                            if (processedPlans == mealPlans.size) {
                                                saveShoppingList(ingredientsMap.values.toList())
                                            }
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            processedPlans++
                            if (processedPlans == mealPlans.size) {
                                saveShoppingList(ingredientsMap.values.toList())
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al obtener planes: ${exception.message}"
                _isLoading.value = false
            }
    }

    // Guardar la lista de compras en Firestore
    private fun saveShoppingList(items: List<ShoppingListItem>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Primero borrar la lista existente
        firestore.collection("users").document(userId)
            .collection("shoppingList")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()

                // Eliminar todos los documentos actuales
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Agregar los nuevos items
                items.forEach { item ->
                    val docRef = firestore.collection("users").document(userId)
                        .collection("shoppingList").document(item.id)

                    // Convertir referencias a formato para Firestore
                    val mealRefsMap = item.mealReferences.map { ref ->
                        mapOf(
                            "mealId" to ref.mealId,
                            "mealName" to ref.mealName,
                            "dayOfWeek" to ref.dayOfWeek,
                            "servings" to ref.servings
                        )
                    }

                    val itemData = hashMapOf(
                        "ingredientName" to item.ingredientName,
                        "quantity" to item.quantity,
                        "unit" to item.unit,
                        "checked" to item.checked,
                        "mealReferences" to mealRefsMap
                    )

                    batch.set(docRef, itemData)
                }

                // Commit el batch
                batch.commit()
                    .addOnSuccessListener {
                        _shoppingItems.value = items
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error al guardar lista: ${e.message}"
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar lista anterior: ${e.message}"
                _isLoading.value = false
            }
    }

    // Actualizar estado (checked) de un item
    fun updateItemCheckedStatus(itemId: String, checked: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        // Actualizar en Firestore
        firestore.collection("users").document(userId)
            .collection("shoppingList").document(itemId)
            .update("checked", checked)
            .addOnSuccessListener {
                // Actualizar localmente
                val currentItems = _shoppingItems.value ?: emptyList()
                val updatedItems = currentItems.map {
                    if (it.id == itemId) it.copy(checked = checked) else it
                }
                _shoppingItems.value = updatedItems
            }
            .addOnFailureListener { e ->
                _error.value = "Error al actualizar item: ${e.message}"
            }
    }

    // Eliminar un item de la lista
    fun removeItem(itemId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        firestore.collection("users").document(userId)
            .collection("shoppingList").document(itemId)
            .delete()
            .addOnSuccessListener {
                // Eliminar del LiveData
                val currentItems = _shoppingItems.value ?: return@addOnSuccessListener
                _shoppingItems.value = currentItems.filter { it.id != itemId }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar item: ${e.message}"
            }
    }

    // Limpiar items marcados como completados
    fun clearCheckedItems() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        val currentItems = _shoppingItems.value ?: return
        val checkedItemIds = currentItems.filter { it.checked }.map { it.id }

        if (checkedItemIds.isEmpty()) return

        val batch = firestore.batch()

        checkedItemIds.forEach { itemId ->
            val docRef = firestore.collection("users").document(userId)
                .collection("shoppingList").document(itemId)
            batch.delete(docRef)
        }

        batch.commit()
            .addOnSuccessListener {
                // Actualizar LiveData
                _shoppingItems.value = currentItems.filter { !it.checked }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar items completados: ${e.message}"
            }
    }
}