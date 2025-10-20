package com.example.migarage.navigation

sealed class Route(val path: String) {
    data object SignIn : Route("sign_in")
    data object Home : Route("home")
    data object AddCar : Route("add_car")


    data object CarDetail : Route("car_detail/{carId}") {
        fun build(carId: String) = "car_detail/$carId"
    }

    data object EditCar : Route("edit_car/{carId}") {
        fun build(carId: String) = "edit_car/$carId"
    }
}

