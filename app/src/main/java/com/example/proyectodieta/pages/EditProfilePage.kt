package com.example.proyectodieta.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.UserProfileViewModel
import com.example.proyectodieta.data.models.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel
) {
    val userProfile by userProfileViewModel.userProfile.observeAsState()

    userProfile?.let { profile ->
        var name by remember { mutableStateOf(profile.name) }
        var email by remember { mutableStateOf(profile.email) }
        var gender by remember { mutableStateOf(profile.gender) }
        var age by remember { mutableStateOf(profile.age.toString()) }
        var weight by remember { mutableStateOf(profile.weight.toString()) }
        var height by remember { mutableStateOf(profile.height.toString()) }
        var fitnessGoal by remember { mutableStateOf(profile.fitnessGoal) }

        var showSaveDialog by remember { mutableStateOf(false) }
        var hasError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Perfil") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                // Validar datos antes de guardar
                                when {
                                    name.isBlank() -> {
                                        hasError = true
                                        errorMessage = "El nombre no puede estar vacío"
                                    }
                                    email.isBlank() -> {
                                        hasError = true
                                        errorMessage = "El correo no puede estar vacío"
                                    }
                                    else -> showSaveDialog = true
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Guardar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información Personal
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown para género
                val genderOptions = listOf("Masculino", "Femenino", "No especificado")
                var expandedGender by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Género") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) age = it },
                    label = { Text("Edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Información Física
                Text(
                    text = "Información Física",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) weight = it },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) height = it },
                    label = { Text("Altura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Objetivo de Fitness
                Text(
                    text = "Objetivo de Fitness",
                    style = MaterialTheme.typography.titleLarge
                )

                // Dropdown para objetivo de fitness
                val fitnessGoalOptions = listOf("Perder peso", "Mantener peso", "Ganar masa muscular", "Mejorar condición física", "Otro")
                var expandedFitnessGoal by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedFitnessGoal,
                    onExpandedChange = { expandedFitnessGoal = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = fitnessGoal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFitnessGoal) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFitnessGoal,
                        onDismissRequest = { expandedFitnessGoal = false }
                    ) {
                        fitnessGoalOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    fitnessGoal = option
                                    expandedFitnessGoal = false
                                }
                            )
                        }
                    }
                }
            }

            // Diálogo de confirmación
            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Guardar cambios") },
                    text = { Text("¿Estás seguro de que deseas guardar los cambios en tu perfil?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Crear un nuevo objeto UserProfile con los datos actualizados
                                val updatedProfile = UserProfile(
                                    email = email,
                                    name = name,
                                    weight = weight.toDoubleOrNull() ?: 0.0,
                                    height = height.toDoubleOrNull() ?: 0.0,
                                    age = age.toIntOrNull() ?: 0,
                                    gender = gender,
                                    fitnessGoal = fitnessGoal
                                )

                                // Actualizar el perfil en el ViewModel
                                userProfileViewModel.updateUserProfile(updatedProfile)

                                // Cerrar el diálogo y volver a la pantalla anterior
                                showSaveDialog = false
                                navController.navigateUp()
                            }
                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Mensaje de error
            if (hasError) {
                AlertDialog(
                    onDismissRequest = { hasError = false },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        Button(onClick = { hasError = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}