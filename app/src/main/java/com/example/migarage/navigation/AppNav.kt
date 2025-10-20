package com.example.migarage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.migarage.ui.addcar.AddCarScreen
import com.example.migarage.ui.home.HomeScreen
import com.example.migarage.ui.signin.SignInScreen

@Composable
fun AppNav(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Route.SignIn.path) {

        composable(Route.SignIn.path) {
            SignInScreen(
                onSignedIn = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.SignIn.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Home.path) {
            HomeScreen(
                onAddCar = { nav.navigate(Route.AddCar.path) },
                onLogout = {
                    nav.navigate(Route.SignIn.path) {
                        popUpTo(Route.Home.path) { inclusive = true } // limpia Home del stack
                    }
                }
            )
        }

        composable(Route.AddCar.path) {
            AddCarScreen(
                onCarSaved = {
                    nav.popBackStack(Route.Home.path, inclusive = false)
                }
            )
        }
    }
}
