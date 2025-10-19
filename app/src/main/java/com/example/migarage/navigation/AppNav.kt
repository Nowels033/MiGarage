package com.example.migarage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.migarage.ui.signin.SignInScreen
import com.example.migarage.ui.home.HomeScreen

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.SignIn.path) {
        composable(Route.SignIn.path) {
            SignInScreen(onSignedIn = {
                navController.navigate(Route.Home.path) {
                    popUpTo(Route.SignIn.path) { inclusive = true }
                }
            })
        }
        composable(Route.Home.path) {
            HomeScreen()
        }
    }
}
