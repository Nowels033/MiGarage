package com.example.migarage.navigation

sealed class Route(val path: String) {
    data object SignIn    : Route("sign_in")
    data object Home      : Route("home")
    data object AddCar    : Route("add_car")

    data object CarDetail : Route("car_detail/{carId}") {
        fun build(carId: String) = "car_detail/$carId"
    }

    data object EditCar   : Route("edit_car/{carId}") {
        fun build(carId: String) = "edit_car/$carId"
    }

    data object MaintList : Route("car_maint/{carId}") {
        fun build(carId: String) = "car_maint/$carId"
    }

    // 👇 mantId opcional como query param (NO segmento de path vacío)
    data object MaintEdit : Route("car_maint_edit/{carId}?maintId={maintId}") {
        fun buildNew(carId: String) = "car_maint_edit/$carId"
        fun buildEdit(carId: String, maintId: String) =
            "car_maint_edit/$carId?maintId=$maintId"
    }
}
