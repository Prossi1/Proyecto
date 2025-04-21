package com.example.proyectodieta.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.UserProfileViewModel
import com.example.proyectodieta.ViewModels.UserProgressViewModel
import com.example.proyectodieta.data.models.UserProgress
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
    userProgressViewModel: UserProgressViewModel
) {
    val userProfile by userProfileViewModel.userProfile.observeAsState()
    val progressEntries by userProgressViewModel.progressEntries.observeAsState(emptyList())
    val isLoading by userProgressViewModel.isLoading.observeAsState(false)
    val error by userProgressViewModel.error.observeAsState()
    val initialWeight by userProgressViewModel.initialWeight.observeAsState(0.0)

    // Estado para el diálogo de añadir progreso
    var showAddProgressDialog by remember { mutableStateOf(false) }

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        userProfileViewModel.checkUserProfile()
        userProgressViewModel.loadProgressEntries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProgressDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir Progreso",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Resumen del progreso
            ProgressSummaryCard(
                initialWeight = initialWeight,
                currentWeight = progressEntries.firstOrNull()?.weight ?: userProfile?.weight ?: 0.0,
                totalEntries = progressEntries.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de entradas de progreso
            Text(
                text = "Historial de progreso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (progressEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay registros de progreso todavía",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn {
                    items(progressEntries) { entry ->
                        ProgressEntryItem(
                            entry = entry,
                            onDelete = { userProgressViewModel.deleteProgressEntry(entry.id) }
                        )
                    }
                }
            }

            // Mostrar error si existe
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    // Diálogo para añadir progreso
    if (showAddProgressDialog) {
        AddProgressDialog(
            onDismiss = { showAddProgressDialog = false },
            onSave = {
                userProgressViewModel.saveProgressEntry()
                showAddProgressDialog = false
            },
            userProgressViewModel = userProgressViewModel
        )
    }
}

@Composable
fun ProgressSummaryCard(
    initialWeight: Double,
    currentWeight: Double,
    totalEntries: Int
) {
    val weightDifference = currentWeight - initialWeight
    val weightChange = if (weightDifference > 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen de progreso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "Peso inicial",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "$initialWeight kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Peso actual",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "$currentWeight kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "Cambio",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "$weightChange${String.format("%.1f", weightDifference)} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            weightDifference < 0 -> Color(0xFF4CAF50) // Verde para pérdida
                            weightDifference > 0 -> Color(0xFFE53935) // Rojo para ganancia
                            else -> Color.Gray // Neutral para sin cambios
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total entradas",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "$totalEntries",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressEntryItem(
    entry: UserProgress,
    onDelete: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormatter.format(entry.date),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Peso: ${entry.weight} kg",
                    fontSize = 14.sp
                )

                if (entry.notes.isNotEmpty()) {
                    Text(
                        text = entry.notes,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (entry.measurements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Medidas:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    entry.measurements.forEach { (name, value) ->
                        Text(
                            text = "$name: $value cm",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
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
fun AddProgressDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    userProgressViewModel: UserProgressViewModel
) {
    val currentProgress by userProgressViewModel.currentProgress.observeAsState()

    // Estados para los campos de medidas
    var waist by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var arms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Registrar Progreso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Peso
                OutlinedTextField(
                    value = if (currentProgress?.weight == 0.0) "" else currentProgress?.weight.toString(),
                    onValueChange = {
                        it.toDoubleOrNull()?.let { weight ->
                            userProgressViewModel.updateWeight(weight)
                        }
                    },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Medidas
                Text(
                    text = "Medidas (cm):",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = waist,
                    onValueChange = {
                        waist = it
                        it.toDoubleOrNull()?.let { measurement ->
                            userProgressViewModel.updateMeasurement("Cintura", measurement)
                        }
                    },
                    label = { Text("Cintura") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chest,
                    onValueChange = {
                        chest = it
                        it.toDoubleOrNull()?.let { measurement ->
                            userProgressViewModel.updateMeasurement("Pecho", measurement)
                        }
                    },
                    label = { Text("Pecho") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = arms,
                    onValueChange = {
                        arms = it
                        it.toDoubleOrNull()?.let { measurement ->
                            userProgressViewModel.updateMeasurement("Brazos", measurement)
                        }
                    },
                    label = { Text("Brazos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        userProgressViewModel.updateNotes(it)
                    },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSave,
                        enabled = currentProgress?.weight ?: 0.0 > 0.0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
