package com.example.proyectodieta.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.MealListViewModel
import com.example.proyectodieta.data.models.Meal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    mealListViewModel: MealListViewModel
) {
    val meals by mealListViewModel.meals.observeAsState(emptyList())
    val isLoading by mealListViewModel.isLoading.observeAsState(false)
    val error by mealListViewModel.error.observeAsState()

    // Estado para controlar el diálogo de confirmación de eliminación
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }

    // Cargar las comidas al entrar en la pantalla
    LaunchedEffect(Unit) {
        mealListViewModel.loadMeals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Comidas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                // Mostrar indicador de carga
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (meals.isEmpty()) {
                // Mostrar mensaje si no hay comidas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No tienes comidas guardadas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("meal_planner") }
                    ) {
                        Text("Crear Nueva Comida")
                    }
                }
            } else {
                // Mostrar la lista de comidas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(meals) { meal ->
                        MealCard(
                            meal = meal,
                            onDeleteClick = { mealToDelete = meal }
                        )
                    }
                }
            }

            // Mostrar mensaje de error si existe
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp)
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar comida
    if (mealToDelete != null) {
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            title = { Text("Eliminar comida") },
            text = { Text("¿Estás seguro de que deseas eliminar ${mealToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mealToDelete?.id?.let { mealListViewModel.deleteMeal(it) }
                        mealToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MealCard(
    meal: Meal,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con nombre y botón de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Información nutricional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(label = "Calorías", value = "${meal.calories} kcal")
                InfoItem(label = "Carbohidratos", value = "${meal.carbs} g")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de ingredientes
            Text(
                "Ingredientes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (meal.ingredients.isEmpty()) {
                Text("No hay ingredientes registrados", color = Color.Gray)
            } else {
                meal.ingredients.forEach { ingredient ->
                    Text("• ${ingredient.quantity} ${ingredient.unit} de ${ingredient.name}")
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
