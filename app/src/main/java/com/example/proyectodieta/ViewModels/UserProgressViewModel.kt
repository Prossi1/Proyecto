package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectodieta.data.models.UserProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserProgressViewModel : ViewModel() {

    private val _progressEntries = MutableLiveData<List<UserProgress>>(emptyList())
    val progressEntries: LiveData<List<UserProgress>> = _progressEntries

    private val _currentProgress = MutableLiveData<UserProgress>(UserProgress())
    val currentProgress: LiveData<UserProgress> = _currentProgress

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _initialWeight = MutableLiveData<Double>(0.0)
    val initialWeight: LiveData<Double> = _initialWeight

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Actualizar peso actual
    fun updateWeight(weight: Double) {
        _currentProgress.value = _currentProgress.value?.copy(weight = weight)
    }

    // Actualizar notas
    fun updateNotes(notes: String) {
        _currentProgress.value = _currentProgress.value?.copy(notes = notes)
    }

    // Añadir o actualizar medida
    fun updateMeasurement(name: String, value: Double) {
        val currentMeasurements = _currentProgress.value?.measurements?.toMutableMap() ?: mutableMapOf()
        currentMeasurements[name] = value
        _currentProgress.value = _currentProgress.value?.copy(measurements = currentMeasurements)
    }

    // Cargar todas las entradas de progreso
    fun loadProgressEntries() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Cargar el peso inicial del perfil del usuario
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val weight = document.getDouble("weight") ?: 0.0
                    _initialWeight.value = weight
                }
            }

        // Cargar entradas de progreso ordenadas por fecha
        firestore.collection("users").document(userId)
            .collection("progress")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val entries = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserProgress::class.java)
                }
                _progressEntries.value = entries
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error al cargar el progreso: ${e.message}"
                _isLoading.value = false
            }
    }

    // Guardar una nueva entrada de progreso y actualizar el perfil
    fun saveProgressEntry() {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        // Crear un calendario con la fecha correcta (20/04/2025)
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.APRIL, 20) // Meses en Calendar son 0-indexed, por lo que abril es 3
        val correctDate = calendar.time

        val progress = _currentProgress.value?.copy(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = correctDate // Usar la fecha correcta
        ) ?: run {
            _error.value = "Error al crear entrada de progreso"
            _isLoading.value = false
            return
        }

        // Referencia al documento del usuario
        val userRef = firestore.collection("users").document(userId)
        // Referencia al documento de progreso
        val progressRef = userRef.collection("progress").document(progress.id)

        // Usar una transacción para actualizar ambos documentos
        firestore.runTransaction { transaction ->
            // Guardar el progreso
            transaction.set(progressRef, progress)

            // Actualizar el peso en el perfil del usuario
            transaction.update(userRef, "weight", progress.weight)
        }.addOnSuccessListener {
            // Añadir la nueva entrada a la lista local
            val currentEntries = _progressEntries.value?.toMutableList() ?: mutableListOf()
            currentEntries.add(0, progress)  // Añadir al principio (más reciente)
            _progressEntries.value = currentEntries

            // Actualizar el peso inicial si es la primera entrada
            if (_initialWeight.value == 0.0) {
                _initialWeight.value = progress.weight
            }

            // Resetear la entrada actual
            _currentProgress.value = UserProgress()
            _isLoading.value = false
        }.addOnFailureListener { e ->
            _error.value = "Error al guardar entrada de progreso: ${e.message}"
            _isLoading.value = false
        }
    }

    // Eliminar una entrada de progreso
    fun deleteProgressEntry(entryId: String) {
        _isLoading.value = true
        _error.value = null

        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            _isLoading.value = false
            return
        }

        firestore.collection("users").document(userId)
            .collection("progress").document(entryId)
            .delete()
            .addOnSuccessListener {
                // Eliminar la entrada de la lista local
                val currentEntries = _progressEntries.value?.toMutableList() ?: mutableListOf()
                val filteredEntries = currentEntries.filter { it.id != entryId }
                _progressEntries.value = filteredEntries

                // Si quedan entradas, actualizar el perfil con el peso más reciente
                if (filteredEntries.isNotEmpty()) {
                    val latestWeight = filteredEntries.first().weight
                    firestore.collection("users").document(userId)
                        .update("weight", latestWeight)
                }

                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar entrada de progreso: ${e.message}"
                _isLoading.value = false
            }
    }

    // Función auxiliar para formatear la fecha correctamente
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}
