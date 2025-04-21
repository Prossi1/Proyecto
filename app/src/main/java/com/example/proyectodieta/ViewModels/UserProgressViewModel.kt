package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectodieta.data.models.UserProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class UserProgressViewModel : ViewModel() {
    private val _progressEntries = MutableLiveData<List<UserProgress>>(emptyList())
    val progressEntries: LiveData<List<UserProgress>> = _progressEntries

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _initialWeight = MutableLiveData<Double>(0.0)
    val initialWeight: LiveData<Double> = _initialWeight

    private val _currentProgress = MutableLiveData<UserProgress>(
        UserProgress(
            id = "",
            weight = 0.0,
            date = Date(),
            measurements = mutableMapOf(),
            notes = ""
        )
    )
    val currentProgress: LiveData<UserProgress?> = _currentProgress

    init {
        resetCurrentProgress()
    }

    fun loadProgressEntries() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            _isLoading.value = true

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val hasInitialWeight = userDoc.contains("initialReferenceWeight")

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .collection("progress")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val entries = querySnapshot.documents.mapNotNull { doc ->
                                val entry = doc.toObject(UserProgress::class.java)
                                entry?.copy(id = doc.id)
                            }

                            _progressEntries.value = entries

                            if (hasInitialWeight) {
                                _initialWeight.value = userDoc.getDouble("initialReferenceWeight") ?: 0.0
                            } else if (entries.isNotEmpty()) {
                                val oldestEntry = entries.minByOrNull { it.date }
                                oldestEntry?.let { oldest ->
                                    _initialWeight.value = oldest.weight
                                    storeInitialReferenceWeight(oldest.weight)
                                }
                            } else {
                                val profileWeight = userDoc.getDouble("weight") ?: 0.0
                                _initialWeight.value = profileWeight
                            }

                            _isLoading.value = false
                        }
                        .addOnFailureListener { exception ->
                            _error.value = exception.localizedMessage ?: "Error al cargar registros de progreso"
                            _isLoading.value = false
                        }
                }
                .addOnFailureListener { exception ->
                    _error.value = exception.localizedMessage ?: "Error al cargar perfil de usuario"
                    _isLoading.value = false
                }
        }
    }

    private fun storeInitialReferenceWeight(weight: Double) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .update("initialReferenceWeight", weight)
                .addOnFailureListener { exception ->
                    _error.value = "Error al guardar peso de referencia: ${exception.message}"
                }
        }
    }

    fun saveProgressEntry() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val progress = _currentProgress.value ?: return

            if (progress.weight <= 0) {
                _error.value = "Por favor ingresa un peso vÃ¡lido"
                return
            }

            _isLoading.value = true

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val hasInitialWeight = userDoc.contains("initialReferenceWeight")

                    if (!hasInitialWeight && (_progressEntries.value?.isEmpty() == true)) {
                        storeInitialReferenceWeight(progress.weight)
                        _initialWeight.value = progress.weight
                    }

                    val progressToSave = progress.copy(date = progress.date ?: Date())

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .collection("progress")
                        .add(progressToSave)
                        .addOnSuccessListener { documentRef ->
                            val updatedEntry = progressToSave.copy(id = documentRef.id)
                            val currentEntries = _progressEntries.value?.toMutableList() ?: mutableListOf()
                            currentEntries.add(0, updatedEntry)
                            _progressEntries.value = currentEntries

                            resetCurrentProgress()
                            _isLoading.value = false
                        }
                        .addOnFailureListener { exception ->
                            _error.value = exception.localizedMessage ?: "Error al guardar progreso"
                            _isLoading.value = false
                        }
                }
                .addOnFailureListener { exception ->
                    _error.value = exception.localizedMessage ?: "Error al verificar peso inicial"
                    _isLoading.value = false
                }
        }
    }

    fun deleteProgressEntry(id: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            _isLoading.value = true

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("progress")
                .document(id)
                .delete()
                .addOnSuccessListener {
                    val currentEntries = _progressEntries.value?.toMutableList() ?: mutableListOf()
                    val updatedEntries = currentEntries.filter { it.id != id }
                    _progressEntries.value = updatedEntries

                    _isLoading.value = false
                }
                .addOnFailureListener { exception ->
                    _error.value = exception.localizedMessage ?: "Error al eliminar registro de progreso"
                    _isLoading.value = false
                }
        }
    }

    fun updateWeight(weight: Double) {
        val current = _currentProgress.value
        current?.let {
            _currentProgress.value = it.copy(weight = weight)
        }
    }

    fun updateMeasurement(name: String, value: Double) {
        val current = _currentProgress.value
        current?.let {
            val updatedMeasurements = it.measurements.toMutableMap()
            updatedMeasurements[name] = value
            _currentProgress.value = it.copy(measurements = updatedMeasurements)
        }
    }

    fun updateNotes(notes: String) {
        val current = _currentProgress.value
        current?.let {
            _currentProgress.value = it.copy(notes = notes)
        }
    }

    private fun resetCurrentProgress() {
        _currentProgress.value = UserProgress(
            id = "",
            weight = 0.0,
            date = Date(),
            measurements = mutableMapOf(),
            notes = ""
        )
    }
}