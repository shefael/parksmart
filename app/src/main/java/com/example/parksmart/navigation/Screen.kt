package com.example.parksmart.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Entry : Screen("entry")
    object Exit : Screen("exit/{sessionId}") {
        fun createRoute(sessionId: String) = "exit/$sessionId"
    }
}