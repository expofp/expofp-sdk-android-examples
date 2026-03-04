package com.expofp.minimap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expofp.minimap.ui.detail.ExhibitorDetailScreen
import com.expofp.minimap.ui.exhibitors.ExhibitorListScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = ExhibitorList
    ) {
        composable<ExhibitorList> {
            ExhibitorListScreen(
                onExhibitorClick = { exhibitorName ->
                    navController.navigate(ExhibitorDetail(exhibitorName = exhibitorName))
                }
            )
        }
        composable<ExhibitorDetail> {
            ExhibitorDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
