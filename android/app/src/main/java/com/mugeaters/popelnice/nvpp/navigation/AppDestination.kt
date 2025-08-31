package com.mugeaters.popelnice.nvpp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.ui.dayslist.DaysListScreen
import com.mugeaters.popelnice.nvpp.ui.home.HomeScreen
import com.mugeaters.popelnice.nvpp.ui.home.HomeViewModel
import com.mugeaters.popelnice.nvpp.ui.settings.SettingsScreen
import com.mugeaters.popelnice.nvpp.ui.trashinfo.TrashInfoScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Enum defining all navigation destinations in the app
 */
sealed class AppDestination(val route: String) {
    object Home : AppDestination("home")
    object Settings : AppDestination("settings")
    object TrashInfo : AppDestination("trash_info/{binType}") {
        fun createRoute(binType: String) = "trash_info/$binType"
    }
    object DaysList : AppDestination("days_list")
}

/**
 * Extension function to set up the navigation graph
 */
fun NavGraphBuilder.appNavGraph(
    navController: NavHostController,
    globalCoordinator: GlobalCoordinator
) {
    composable(AppDestination.Home.route) {
        HomeScreen(
            onSettingsClick = { days ->
                globalCoordinator.navigateToSettings(days)
            },
            onInfoClick = { binType ->
                globalCoordinator.navigateToTrashInfo(binType)
            },
            onCalendarClick = { days ->
                globalCoordinator.navigateToDaysList(days)
            },
            onSortingGuideClick = {
                globalCoordinator.openSortingGuide()
            }
        )
    }
    
    composable(AppDestination.Settings.route) {
        val days = (globalCoordinator as? GlobalCoordinatorImpl)?.getCurrentDays() ?: emptyList()
        SettingsScreen(
            days = days,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
    
    composable(AppDestination.TrashInfo.route) { backStackEntry ->
        val binType = backStackEntry.arguments?.getString("binType") ?: "plastic"
        val homeViewModel: HomeViewModel = koinViewModel()
        
        val sections = when (binType) {
            "plastic" -> homeViewModel.plasticTrashInfoSection()
            "paper" -> homeViewModel.paperTrashInfoSection()
            "bio" -> homeViewModel.bioTrashInfoSection()
            "mix" -> homeViewModel.mixTrashInfoSection()
            "heavyLoad" -> homeViewModel.heavyLoadInfoSection()
            else -> homeViewModel.plasticTrashInfoSection()
        }
        
        TrashInfoScreen(
            sections = sections,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
    
    composable(AppDestination.DaysList.route) {
        // For now, we'll get the days from the global coordinator
        // In a production app, you might use navigation arguments or a shared ViewModel
        val days = (globalCoordinator as? GlobalCoordinatorImpl)?.getCurrentDays() ?: emptyList()
        DaysListScreen(
            days = days,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}