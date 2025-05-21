package cz.novavesodpad.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavHostController
import cz.novavesodpad.model.TrashDay

/**
 * Coordinator interface that handles navigation between screens
 */
interface GlobalCoordinator {
    fun navigateToHome()
    fun navigateToSettings(days: List<TrashDay>)
    fun navigateToTrashInfo()
    fun goToSystemSettings()
    fun openWeb()
}

/**
 * Implementation of global coordinator
 */
class GlobalCoordinatorImpl(
    private val context: Context,
    private val navController: NavHostController
) : GlobalCoordinator {
    
    override fun navigateToHome() {
        navController.navigate(AppDestination.Home.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }
    
    override fun navigateToSettings(days: List<TrashDay>) {
        // In a real app, you might want to pass the days via a NavArg or ViewModel
        navController.navigate(AppDestination.Settings.route)
    }
    
    override fun navigateToTrashInfo() {
        navController.navigate(AppDestination.TrashInfo.route)
    }
    
    override fun goToSystemSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    override fun openWeb() {
        val url = "https://www.novaves.cz/odpady/ds-1192/p1=1985"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}