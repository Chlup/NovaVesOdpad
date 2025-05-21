package cz.novavesodpad.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.ui.home.HomeScreen
import cz.novavesodpad.ui.settings.SettingsScreen
import cz.novavesodpad.ui.trashinfo.TrashInfoScreen

/**
 * Enum defining all navigation destinations in the app
 */
sealed class AppDestination(val route: String) {
    object Home : AppDestination("home")
    object Settings : AppDestination("settings")
    object TrashInfo : AppDestination("trash_info")
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
            onInfoClick = {
                globalCoordinator.navigateToTrashInfo()
            }
        )
    }
    
    composable(AppDestination.Settings.route) {
        SettingsScreen(
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
    
    composable(AppDestination.TrashInfo.route) {
        TrashInfoScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onWebClick = {
                globalCoordinator.openWeb()
            }
        )
    }
}