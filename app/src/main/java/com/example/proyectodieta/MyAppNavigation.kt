package com.example.proyectodieta

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectodieta.ViewModels.AuthViewModel
import com.example.proyectodieta.ViewModels.MealListViewModel
import com.example.proyectodieta.ViewModels.MealPlannerViewModel
import com.example.proyectodieta.ViewModels.ShoppingListViewModel
import com.example.proyectodieta.ViewModels.UserProfileViewModel
import com.example.proyectodieta.ViewModels.UserProgressViewModel
import com.example.proyectodieta.ViewModels.WeeklyScheduleViewModel
import com.example.proyectodieta.pages.EditProfilePage
import com.example.proyectodieta.pages.HelpPage
import com.example.proyectodieta.pages.Homepage
import com.example.proyectodieta.pages.LoginPage
import com.example.proyectodieta.pages.MealListPage
import com.example.proyectodieta.pages.MealPlannerPage
import com.example.proyectodieta.pages.ProfileDetailsPage
import com.example.proyectodieta.pages.ProgressPage
import com.example.proyectodieta.pages.ShoppingListPage
import com.example.proyectodieta.pages.SignupPage
import com.example.proyectodieta.pages.UserProfileCreationPage
import com.example.proyectodieta.pages.WeeklySchedulePage

@Composable
fun MyAppNavigation(
   modifier: Modifier = Modifier,
   authViewModel: AuthViewModel,
   userProfileViewModel: UserProfileViewModel,
   userProgressViewModel: UserProgressViewModel

) {
   val navController = rememberNavController()

   NavHost(navController = navController, startDestination = "login", builder = {
      composable("login") {
         LoginPage(modifier, navController, authViewModel)
      }
      composable("signup") {
         SignupPage(modifier, navController, authViewModel)
      }
      composable("home") {
         Homepage(modifier, navController, authViewModel, userProfileViewModel)
      }
      composable("create_profile") {
         UserProfileCreationPage(modifier, navController, userProfileViewModel)
      }
      composable("profile_details") {
         ProfileDetailsPage(navController, userProfileViewModel)
      }
      composable("help") {
         HelpPage(modifier, navController)
      }
      composable("meal_planner") {
         val mealPlannerViewModel: MealPlannerViewModel = viewModel()
         MealPlannerPage(modifier, navController, mealPlannerViewModel)
      }
      composable("meal_list") {
         val mealListViewModel: MealListViewModel = viewModel()
         MealListPage(modifier, navController, mealListViewModel)
      }
      composable("weekly_schedule") {
         val weeklyScheduleViewModel: WeeklyScheduleViewModel = viewModel()
         WeeklySchedulePage(navController, weeklyScheduleViewModel)
      }
      composable("shopping_list") {
         val shoppingListViewModel = viewModel<ShoppingListViewModel>()
         ShoppingListPage(navController = navController, shoppingListViewModel = shoppingListViewModel
         )
      }
      composable("edit_profile") {
         EditProfilePage(navController, userProfileViewModel)
      }
      composable("progress") {
         ProgressPage(
            navController = navController,
            userProfileViewModel = userProfileViewModel,
            userProgressViewModel = userProgressViewModel
         )
      }

   })
}