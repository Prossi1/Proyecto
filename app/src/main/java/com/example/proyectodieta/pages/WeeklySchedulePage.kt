package com.example.proyectodieta.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.KeyboardType
import com.example.proyectodieta.ViewModels.WeeklyScheduleViewModel
import com.example.proyectodieta.data.models.ScheduledMeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySchedulePage(
    navController: NavController,
    weeklyScheduleViewModel: WeeklyScheduleViewModel
) {
    val mealPlans by weeklyScheduleViewModel.mealPlans.observeAsState(emptyList())
    val isLoading by weeklyScheduleViewModel.isLoading.observeAsState(false)
    val error by weeklyScheduleViewModel.error.observeAsState(null)

    // Días de la semana
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    // Estado para controlar el diálogo de añadir comida
    var showAddMealDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("") }

    // Cargar los planes de comida al entrar a la pantalla
    LaunchedEffect(Unit) {
        weeklyScheduleViewModel.loadWeeklySchedule()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda Semanal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Por defecto, seleccionar el primer día si no hay ninguno seleccionado
                    if (selectedDay.isEmpty() && daysOfWeek.isNotEmpty()) {
                        selectedDay = daysOfWeek[0]
                    }
                    showAddMealDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir comida")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                mealPlans.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay comidas programadas",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedDay = daysOfWeek[0]
                                showAddMealDialog = true
                            }
                        ) {
                            Text("Programar primera comida")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Mostrar cada día de la semana
                        daysOfWeek.forEach { day ->
                            item {
                                DayHeader(
                                    day = day,
                                    onAddClick = {
                                        selectedDay = day
                                        showAddMealDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Encontrar el plan de comida para este día
                            val dayPlan = mealPlans.find { it.dayOfWeek == day }

                            if (dayPlan != null && dayPlan.meals.isNotEmpty()) {
                                items(dayPlan.meals) { scheduledMeal ->
                                    MealCard(
                                        scheduledMeal = scheduledMeal,
                                        onDeleteClick = {
                                            weeklyScheduleViewModel.removeScheduledMeal(
                                                dayOfWeek = day,
                                                mealId = scheduledMeal.id
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            } else {
                                item {
                                    Text(
                                        text = "No hay comidas programadas para este día",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                                    )
                                }
                            }

                            item {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para añadir nueva comida
    if (showAddMealDialog) {
        AddMealDialog(
            day = selectedDay,
            onDismiss = { showAddMealDialog = false },
            onAddMeal = { mealId, mealName, timeOfDay, servings ->
                weeklyScheduleViewModel.addScheduledMeal(
                    dayOfWeek = selectedDay,
                    mealId = mealId,
                    mealName = mealName,
                    timeOfDay = timeOfDay,
                    servings = servings
                )
                showAddMealDialog = false
            },
            weeklyScheduleViewModel = weeklyScheduleViewModel
        )
    }
}

@Composable
fun DayHeader(
    day: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        IconButton(onClick = onAddClick) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Añadir comida",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun MealCard(
    scheduledMeal: ScheduledMeal,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scheduledMeal.mealName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scheduledMeal.timeOfDay,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Porciones: ${scheduledMeal.servings}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    day: String,
    onDismiss: () -> Unit,
    onAddMeal: (String, String, String, Int) -> Unit,
    weeklyScheduleViewModel: WeeklyScheduleViewModel
) {
    val meals by weeklyScheduleViewModel.availableMeals.observeAsState(emptyList())
    var selectedMealId by remember { mutableStateOf("") }
    var selectedMealName by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("Desayuno") }
    var servings by remember { mutableStateOf("1") }

    val timeOptions = listOf("Desayuno", "Almuerzo", "Merienda", "Cena", "Snack")

    // Cargar las comidas disponibles al abrir el diálogo
    LaunchedEffect(Unit) {
        weeklyScheduleViewModel.loadAvailableMeals()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir comida para $day") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (meals.isEmpty()) {
                    Text(
                        "No hay comidas disponibles. Crea algunas comidas primero.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Selecciona una comida:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropdown para seleccionar comida
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        TextField(
                            value = selectedMealName.ifEmpty { "Selecciona una comida" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            meals.forEach { meal ->
                                DropdownMenuItem(
                                    text = { Text(meal.name) },
                                    onClick = {
                                        selectedMealId = meal.id
                                        selectedMealName = meal.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dropdown para momento del día
                    Text("Momento del día:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    var timeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = timeExpanded,
                        onExpandedChange = { timeExpanded = it }
                    ) {
                        TextField(
                            value = timeOfDay,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = timeExpanded,
                            onDismissRequest = { timeExpanded = false }
                        ) {
                            timeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        timeOfDay = option
                                        timeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo para número de porciones
                    Text("Número de porciones:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = servings,
                        onValueChange = { input ->
                            // Solo permitir números
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                servings = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMealId.isNotEmpty()) {
                        onAddMeal(
                            selectedMealId,
                            selectedMealName,
                            timeOfDay,
                            servings.toIntOrNull() ?: 1
                        )
                    }
                },
                enabled = selectedMealId.isNotEmpty() && meals.isNotEmpty()
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}