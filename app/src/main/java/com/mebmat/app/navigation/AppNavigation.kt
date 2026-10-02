package com.mebmat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mebmat.app.ui.screens.AnalysisScreen
import com.mebmat.app.ui.screens.HomeScreen
import com.mebmat.app.ui.screens.MaterialSelectScreen
import com.mebmat.app.ui.screens.ResultScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onMaterialSelectClick = {
                    navController.navigate("material_select")
                }
            )
        }

        composable("material_select") {
            MaterialSelectScreen(
                onAnalyzeClick = {
                    navController.navigate("analysis")
                }
            )
        }

        composable("analysis") {
            AnalysisScreen(
                onShowResultClick = {
                    navController.navigate("result")
                }
            )
        }

        composable("result") {
            ResultScreen()
        }
    }
}