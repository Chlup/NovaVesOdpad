package com.mugeaters.popelnice.nvpp.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavHostController
import com.mugeaters.popelnice.nvpp.model.TrashDay

/**
 * Coordinator interface that handles navigation between screens
 */
interface GlobalCoordinator {
    fun navigateToHome()
    fun navigateToSettings(days: List<TrashDay>)
    fun navigateToTrashInfo(binType: String)
    fun navigateToDaysList(days: List<TrashDay>)
    fun goToSystemSettings()
    fun openWeb()
    fun openSortingGuide()
}

/**
 * Implementation of global coordinator
 */
class GlobalCoordinatorImpl(
    private val context: Context,
    private val navController: NavHostController
) : GlobalCoordinator {
    
    // Temporary storage for days - in production, consider using a proper state management solution
    private var currentDays: List<TrashDay> = emptyList()
    
    fun getCurrentDays(): List<TrashDay> = currentDays
    
    override fun navigateToHome() {
        navController.navigate(AppDestination.Home.route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }
    
    override fun navigateToSettings(days: List<TrashDay>) {
        currentDays = days
        navController.navigate(AppDestination.Settings.route)
    }
    
    override fun navigateToTrashInfo(binType: String) {
        navController.navigate(AppDestination.TrashInfo.createRoute(binType))
    }
    
    override fun navigateToDaysList(days: List<TrashDay>) {
        currentDays = days
        navController.navigate(AppDestination.DaysList.route)
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
    
    override fun openSortingGuide() {
        val url = "https://www.jaktridit.cz"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}