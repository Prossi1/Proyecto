package com.example.proyectodieta.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.planificadordietas.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileViewModel : ViewModel() {
    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    sealed class ProfileState {
        object Initial : ProfileState()
        object Loading : ProfileState()
        object Success : ProfileState()
        object ProfileExists : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    fun saveUserProfile(
        name: String,
        weight: Double,
        height: Double,
        age: Int,
        gender: String,
        fitnessGoal: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userProfile = UserProfile(
                email = user.email ?: "",
                name = name,
                weight = weight,
                height = height,
                age = age,
                gender = gender,
                fitnessGoal = fitnessGoal
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(userProfile)
                .addOnSuccessListener {
                    _profileState.value = ProfileState.Success
                    _userProfile.value = userProfile
                }
                .addOnFailureListener { exception ->
                    _profileState.value = ProfileState.Error(
                        exception.localizedMessage ?: "Failed to save profile"
                    )
                }
        } ?: run {
            _profileState.value = ProfileState.Error("No authenticated user found")
        }
    }

    fun checkUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            _profileState.value = ProfileState.Loading

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                        _profileState.value = ProfileState.ProfileExists
                    } else {
                        _profileState.value = ProfileState.Initial
                    }
                }
                .addOnFailureListener { exception ->
                    _profileState.value = ProfileState.Error(
                        exception.localizedMessage ?: "Failed to retrieve profile"
                    )
                }
        }
    }
}

