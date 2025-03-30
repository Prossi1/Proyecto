package com.example.proyectodieta.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpPage(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayuda del Sistema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de introducción
            Text(
                "Manual de Usuario",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                "¡Bienvenido al Planificador de Dietas Personalizado! Esta aplicación te ayudará a gestionar tu alimentación de acuerdo a tus objetivos nutricionales.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tarjeta de funcionalidad principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Funcionalidades Principales",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Planificar Comidas
                    Text(
                        "Planificar Comidas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        "Esta función te permite crear y organizar tus planes de alimentación. Puedes:" +
                                "\n• Crear planes diarios, semanales o mensuales" +
                                "\n• Registrar platos con sus ingredientes" +
                                "\n• Calcular calorías y macronutrientes automáticamente" +
                                "\n• Recibir sugerencias basadas en tus objetivos",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Lista de Compras
                    Text(
                        "Lista de Compras",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        "Genera automáticamente una lista de ingredientes necesarios para tus comidas planificadas:" +
                                "\n• Lista organizada por categorías de alimentos" +
                                "\n• Cantidades precisas de cada ingrediente" +
                                "\n• Posibilidad de editar y añadir artículos" +
                                "\n• Marcado de artículos ya comprados",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Agenda
                    Text(
                        "Agenda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        "Lleva un registro de tu progreso y actividades relacionadas con tu plan nutricional:" +
                                "\n• Seguimiento diario de comidas consumidas" +
                                "\n• Registro de peso y medidas corporales" +
                                "\n• Estadísticas de avance hacia tus objetivos" +
                                "\n• Recordatorios personalizados",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de primeros pasos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Primeros Pasos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "1. Completa tu perfil con información personal y objetivos nutricionales",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "2. Explora las diferentes secciones desde la pantalla principal",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "3. Comienza creando tu primer plan de alimentación en \"Planificar Comidas\"",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "4. Genera tu lista de compras automáticamente",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "5. Registra tu progreso diario en la \"Agenda\"",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de consejos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Consejos y Recomendaciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "• Actualiza tu peso regularmente para ajustar cálculos nutricionales",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "• Utiliza la función de reconocimiento de alimentos para agilizar el registro",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "• Revisa las estadísticas semanalmente para evaluar tu progreso",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "• Configura notificaciones para mantener constancia en tus hábitos",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "• Saca fotos de tus comidas para un registro más detallado",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Versión 1.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}