package com.example.proyectodieta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.proyectodieta.ViewModels.AuthViewModel
import com.example.proyectodieta.ViewModels.UserProfileViewModel
import com.example.proyectodieta.ui.theme.PlanificadorDietasTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()
        val userProfileViewModel: UserProfileViewModel by viewModels()

        setContent {
            PlanificadorDietasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        userProfileViewModel = userProfileViewModel
                    )
                }
            }
        }
    }
}