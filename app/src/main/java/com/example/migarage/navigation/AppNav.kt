package com.example.migarage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.migarage.ui.addcar.AddCarScreen
import com.example.migarage.ui.cardetail.CarDetailScreen
import com.example.migarage.ui.editcar.EditCarScreen
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
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                },
                // 👇 ahora abre DETALLE
                onCarClick = { carId -> nav.navigate(Route.CarDetail.build(carId)) }
            )
        }

        composable(Route.AddCar.path) {
            AddCarScreen(onCarSaved = { nav.popBackStack(Route.Home.path, false) })
        }

        // 🆕 Detalle solo lectura
        composable(
            route = Route.CarDetail.path,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStack ->
            val carId = backStack.arguments?.getString("carId") ?: return@composable
            CarDetailScreen(
                carId = carId,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Route.EditCar.build(carId)) }
            )
        }

        // Editor existente
        composable(
            route = Route.EditCar.path,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStack ->
            val carId = backStack.arguments?.getString("carId") ?: return@composable
            EditCarScreen(
                carId = carId,
                onSaved = { nav.popBackStack() },
                onDeleted = { nav.popBackStack(Route.Home.path, false) }
            )
        }
    }
}
