package com.example.proyectodieta.pages

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.MealPlannerViewModel
import com.example.proyectodieta.data.models.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    mealPlannerViewModel: MealPlannerViewModel
) {
    val currentMeal by mealPlannerViewModel.currentMeal.observeAsState()
    val ingredients by mealPlannerViewModel.ingredients.observeAsState(emptyList())
    val isLoading by mealPlannerViewModel.isLoading.observeAsState(false)
    val error by mealPlannerViewModel.error.observeAsState()

    // Ingrediente en estado de edición temporal
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }

    // Estado temporal para los valores editados
    var tempName by remember { mutableStateOf("") }
    var tempQuantity by remember { mutableStateOf("") }
    var tempUnit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planificar Comidas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Campo de nombre de la comida
                OutlinedTextField(
                    value = currentMeal?.name ?: "",
                    onValueChange = { mealPlannerViewModel.updateMealName(it) },
                    label = { Text("Nombre de la comida") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Campos para calorías y carbohidratos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (currentMeal?.calories == 0) "" else currentMeal?.calories.toString(),
                        onValueChange = {
                            mealPlannerViewModel.updateCalories(it.toIntOrNull() ?: 0)
                        },
                        label = { Text("Calorías") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = if (currentMeal?.carbs == 0) "" else currentMeal?.carbs.toString(),
                        onValueChange = {
                            mealPlannerViewModel.updateCarbs(it.toIntOrNull() ?: 0)
                        },
                        label = { Text("Carbohidratos (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    "Ingredientes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Cabecera de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nombre", modifier = Modifier.weight(2f))
                    Text("Cantidad", modifier = Modifier.weight(1f))
                    Text("Unidad", modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(48.dp)) // Espacio para los botones
                }
            }

            // Lista de ingredientes
            items(ingredients) { ingredient ->
                // Si este ingrediente está en modo edición
                if (editingIngredient?.id == ingredient.id) {
                    // Fila en modo edición
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Campo de nombre
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            label = { Text("Nombre") },
                            modifier = Modifier.weight(2f)
                        )

                        // Campo de cantidad
                        OutlinedTextField(
                            value = tempQuantity,
                            onValueChange = { tempQuantity = it },
                            singleLine = true,
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f)
                        )

                        // Campo de unidad
                        OutlinedTextField(
                            value = tempUnit,
                            onValueChange = { tempUnit = it },
                            singleLine = true,
                            label = { Text("Unidad") },
                            modifier = Modifier.weight(1f)
                        )

                        // Botón para guardar
                        IconButton(
                            onClick = {
                                // Guardar cambios
                                val updated = ingredient.copy(
                                    name = tempName,
                                    quantity = tempQuantity,
                                    unit = tempUnit
                                )
                                mealPlannerViewModel.updateIngredient(updated)
                                editingIngredient = null
                            }
                        ) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = "Guardar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Fila en modo visualización normal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                            .clickable {
                                // Preparar para edición
                                editingIngredient = ingredient
                                tempName = ingredient.name
                                tempQuantity = ingredient.quantity
                                tempUnit = ingredient.unit
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mostrar datos
                        Text(
                            text = ingredient.name.ifEmpty { "Click para editar" },
                            modifier = Modifier.weight(2f)
                        )

                        Text(
                            text = ingredient.quantity.ifEmpty { "Click" },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = ingredient.unit.ifEmpty { "Click" },
                            modifier = Modifier.weight(1f)
                        )

                        // Botón para eliminar
                        IconButton(
                            onClick = { mealPlannerViewModel.removeIngredient(ingredient.id) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar ingrediente",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Botón para agregar ingrediente
            item {
                Button(
                    onClick = { mealPlannerViewModel.addIngredient() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar ingrediente",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Agregar Ingrediente")
                }
            }

            // Mensaje de error si existe
            item {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Botón de guardado
            item {
                Button(
                    onClick = { mealPlannerViewModel.saveMeal() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && currentMeal?.name?.isNotEmpty() == true && ingredients.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Guardar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Guardar Comida")
                    }
                }
            }
        }
    }
}