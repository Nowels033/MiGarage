package com.example.migarage.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.migarage.ui.addcar.AddCarScreen
import com.example.migarage.ui.home.HomeScreen
import com.example.migarage.ui.signin.SignInScreen

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.SignIn.path) {

        // 🔵 Pantalla de inicio de sesión
        composable(Route.SignIn.path) {
            SignInScreen(onSignedIn = {
                navController.navigate(Route.Home.path) {
                    popUpTo(Route.SignIn.path) { inclusive = true }
                }
            })
        }

        // 🔵 Pantalla principal (Home)
        composable(Route.Home.path) {
            HomeScreen(
                onAddCar = { navController.navigate(Route.AddCar.path) } // 👈 aquí el botón “+”
            )
        }

        // 🆕 Pantalla para añadir coche
        composable(Route.AddCar.path) {
            AddCarScreen(
                onCarSaved = {
                    // vuelve atrás después de guardar
                    navController.popBackStack(Route.Home.path, inclusive = false)
                }
            )
        }
    }
}
