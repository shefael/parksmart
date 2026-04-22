package com.example.parksmart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.parksmart.navigation.Screen
import com.example.parksmart.screens.dashboard.DashboardScreen
import com.example.parksmart.screens.entry.EntryScreen
import com.example.parksmart.screens.exit.ExitScreen

@Composable
fun ParkSmartNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToEntry = {
                    navController.navigate(Screen.Entry.route)
                },
                onNavigateToExit = { sessionId ->
                    // ✅ On passe UNIQUEMENT l'ID (String), pas l'objet !
                    navController.navigate(Screen.Exit.createRoute(sessionId))
                }
            )
        }

        composable(route = Screen.Entry.route) {
            EntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Exit.route,
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ExitScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}