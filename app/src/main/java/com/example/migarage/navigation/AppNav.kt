package com.example.migarage.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.migarage.ui.addcar.AddCarScreen
import com.example.migarage.ui.cardetail.CarDetailScreen
import com.example.migarage.ui.editcar.EditCarScreen
import com.example.migarage.ui.home.HomeScreen
import com.example.migarage.ui.maintenance.AddEditMaintenanceScreen
import com.example.migarage.ui.maintenance.MaintenanceListScreen
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
                onCarClick = { carId -> nav.navigate(Route.CarDetail.build(carId)) }
            )
        }

        composable(Route.AddCar.path) {
            AddCarScreen(onCarSaved = { nav.popBackStack(Route.Home.path, false) })
        }

        composable(
            route = Route.CarDetail.path,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { back ->
            val carId = back.arguments?.getString("carId") ?: return@composable
            CarDetailScreen(
                carId = carId,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Route.EditCar.build(carId)) },
                onMaint = { nav.navigate(Route.MaintList.build(carId)) }
            )
        }

        composable(
            route = Route.EditCar.path,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { back ->
            val carId = back.arguments?.getString("carId") ?: return@composable
            EditCarScreen(
                carId = carId,
                onSaved = { nav.popBackStack() },
                onDeleted = { nav.popBackStack(Route.Home.path, false) }
            )
        }

        // LISTA
        composable(
            route = Route.MaintList.path,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { back ->
            val carId = back.arguments?.getString("carId") ?: return@composable
            MaintenanceListScreen(
                carId = carId,
                onBack = { nav.popBackStack() },
                onAdd  = { nav.navigate(Route.MaintEdit.buildNew(carId)) },
                onEdit = { maintId -> nav.navigate(Route.MaintEdit.buildEdit(carId, maintId)) }
            )
        }

        // ADD/EDIT con mantId opcional (query)
        composable(
            route = Route.MaintEdit.path,
            arguments = listOf(
                navArgument("carId")   { type = NavType.StringType },
                navArgument("maintId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { back ->
            val carId   = back.arguments?.getString("carId") ?: return@composable
            val maintId = back.arguments?.getString("maintId")
            AddEditMaintenanceScreen(
                carId = carId,
                maintId = maintId,
                onDone  = { nav.popBackStack() }
            )
        }
    }
}
