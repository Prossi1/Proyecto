package com.example.proyectodieta.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.UserProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfileViewModel: UserProfileViewModel
) {
    val userProfile by userProfileViewModel.userProfile.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // TODO: Implementar edición de perfil
                        // navController.navigate("edit_profile")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil")
                    }
                }
            )
        }
    ) { innerPadding ->
        userProfile?.let { profile ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Información Personal
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProfileDetailItem("Nombre", profile.name)
                ProfileDetailItem("Correo Electrónico", profile.email)
                ProfileDetailItem("Género", profile.gender)
                ProfileDetailItem("Edad", profile.age.toString())

                // Información Física
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Información Física",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProfileDetailItem("Peso", "${profile.weight} kg")
                ProfileDetailItem("Altura", "${profile.height} cm")

                // Objetivo de Fitness
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Objetivo de Fitness",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProfileDetailItem("Meta", profile.fitnessGoal)
            }
        } ?: run {
            // Manejo si no hay perfil
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró información de perfil",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}