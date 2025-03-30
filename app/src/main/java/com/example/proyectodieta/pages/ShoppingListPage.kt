package com.example.proyectodieta.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectodieta.ViewModels.ShoppingListViewModel
import com.example.proyectodieta.data.models.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListPage(
    navController: NavController,
    shoppingListViewModel: ShoppingListViewModel
) {
    val shoppingItems by shoppingListViewModel.shoppingItems.observeAsState(emptyList())
    val isLoading by shoppingListViewModel.isLoading.observeAsState(false)
    val error by shoppingListViewModel.error.observeAsState(null)

    // Estado para controlar diálogo de información de un item
    var selectedItemInfo by remember { mutableStateOf<ShoppingListItem?>(null) }

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        shoppingListViewModel.loadShoppingList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { shoppingListViewModel.generateShoppingList() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generar Lista")
                    }
                    IconButton(onClick = { shoppingListViewModel.clearCheckedItems() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Marcados")
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
            // Contenido principal
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (shoppingItems.isEmpty()) {
                EmptyShoppingList(
                    onGenerateClick = { shoppingListViewModel.generateShoppingList() }
                )
            } else {
                ShoppingItemsList(
                    items = shoppingItems,
                    onItemClick = { item -> selectedItemInfo = item },
                    onCheckedChange = { item, checked ->
                        shoppingListViewModel.updateItemCheckedStatus(item.id, checked)
                    }
                )
            }

            // Diálogo de información del item
            selectedItemInfo?.let { item ->
                ShoppingItemInfoDialog(
                    item = item,
                    onDismiss = { selectedItemInfo = null }
                )
            }
        }
    }
}

@Composable
fun EmptyShoppingList(onGenerateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No hay ingredientes en la lista de compras",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onGenerateClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Generar",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Generar desde Agenda")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Programa comidas en la agenda para generar una lista automáticamente",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 32.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShoppingItemsList(
    items: List<ShoppingListItem>,
    onItemClick: (ShoppingListItem) -> Unit,
    onCheckedChange: (ShoppingListItem, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ShoppingItemRow(
                item = item,
                onItemClick = onItemClick,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingListItem,
    onItemClick: (ShoppingListItem) -> Unit,
    onCheckedChange: (ShoppingListItem, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { checked -> onCheckedChange(item, checked) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = item.ingredientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${item.quantity.formatQuantity()} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Indicador de referencias
            if (item.mealReferences.isNotEmpty()) {
                IconButton(onClick = { onItemClick(item) }) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(text = "${item.mealReferences.size}")
                    }
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver detalles",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemInfoDialog(
    item: ShoppingListItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${item.ingredientName} (${item.quantity.formatQuantity()} ${item.unit})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Este ingrediente es necesario para:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                item.mealReferences.forEach { ref ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = ref.mealName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${ref.dayOfWeek} (${ref.servings} porciones)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// Extensión para formatear cantidades
fun Double.formatQuantity(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}