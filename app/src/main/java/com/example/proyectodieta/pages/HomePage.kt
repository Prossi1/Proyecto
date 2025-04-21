package com.example.proyectodieta.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectodieta.R
import com.example.proyectodieta.ViewModels.AuthState
import com.example.proyectodieta.ViewModels.AuthViewModel
import com.example.proyectodieta.ViewModels.UserProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homepage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val profileState by userProfileViewModel.profileState.observeAsState()

    // Estado para el Side Drawer
    var isSideDrawerVisible by remember { mutableStateOf(false) }

    // Verificar perfil del usuario al entrar a la pantalla
    LaunchedEffect(Unit) {
        userProfileViewModel.checkUserProfile()
    }

    // Verificación de autenticación
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Planificador de dietas") },
                    navigationIcon = {
                        IconButton(onClick = { isSideDrawerVisible = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("profile_details") }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (profileState) {
                    is UserProfileViewModel.ProfileState.Initial -> {
                        Button(
                            onClick = { navController.navigate("create_profile") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Crear Perfil")
                        }
                    }

                    is UserProfileViewModel.ProfileState.ProfileExists -> {
                        // Reemplazar el texto de bienvenida por una imagen larga
                        Image(
                            painter = painterResource(id = R.drawable.download),
                            contentDescription = "Banner de bienvenida",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(vertical = 8.dp),
                            contentScale = ContentScale.FillWidth
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { navController.navigate("meal_planner") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Planificar Comidas",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Planificar Comidas")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { navController.navigate("meal_list") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Planificar Comidas",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Lista de comidas registradas")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { navController.navigate("shopping_list") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Lista de Compras",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Lista de Compras")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { navController.navigate("weekly_schedule") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Agenda",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Agenda")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nuevo botón para Ver Progreso
                            Button(
                                onClick = { navController.navigate("progress") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    Icons.Default.ShowChart,
                                    contentDescription = "Ver Progreso",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Ver Progreso")
                            }
                        }

                        Button(
                            onClick = { navController.navigate("help") },
                            modifier = Modifier
                                .padding(bottom = 24.dp, top = 16.dp)
                                .width(200.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Ayuda",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Ayuda del Sistema")
                        }
                    }

                    is UserProfileViewModel.ProfileState.Error -> {
                        Text("Error al cargar perfil", color = MaterialTheme.colorScheme.error)
                    }

                    else -> CircularProgressIndicator()
                }
            }
        }

        // Fondo oscurecido para el menú lateral
        AnimatedVisibility(
            visible = isSideDrawerVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isSideDrawerVisible = false }
            )
        }

        // Menú lateral
        AnimatedVisibility(
            visible = isSideDrawerVisible,
            enter = slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { -it }
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { -it }
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Opciones", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            navController.navigate("profile_details")
                            isSideDrawerVisible = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Detalles de Perfil")
                    }


                    TextButton(
                        onClick = {
                            authViewModel.signout()
                            isSideDrawerVisible = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    }
}
