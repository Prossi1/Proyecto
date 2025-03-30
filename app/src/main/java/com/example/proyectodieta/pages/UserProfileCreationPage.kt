package com.example.proyectodieta.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.planificadordietas.ViewModels.UserProfileViewModel
import com.example.planificadordietas.ViewModels.UserProfileViewModel.ProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileCreationPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfileViewModel: UserProfileViewModel
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var fitnessGoal by remember { mutableStateOf("") }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Hombre", "Mujer", "Prefiero no decirlo")

    val profileState by userProfileViewModel.profileState.observeAsState(initial = ProfileState.Initial)
    val context = LocalContext.current

    // Handle profile state changes
    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileState.Success -> {
                // Navigate to main app or dashboard
                navController.navigate("home")
            }
            is ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete Your Profile",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Name TextField
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weight TextField
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Height TextField
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Age TextField
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                label = { Text("Gender") },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fitness Goal TextField
        OutlinedTextField(
            value = fitnessGoal,
            onValueChange = { fitnessGoal = it },
            label = { Text("Fitness Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Profile Button
        Button(
            onClick = {
                // Validate and save profile
                userProfileViewModel.saveUserProfile(
                    name = name,
                    weight = weight.toDoubleOrNull() ?: 0.0,
                    height = height.toDoubleOrNull() ?: 0.0,
                    age = age.toIntOrNull() ?: 0,
                    gender = gender,
                    fitnessGoal = fitnessGoal
                )
            },
            enabled = name.isNotBlank() &&
                    weight.isNotBlank() &&
                    height.isNotBlank() &&
                    age.isNotBlank() &&
                    gender.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}