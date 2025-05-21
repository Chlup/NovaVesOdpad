package cz.novavesodpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cz.novavesodpad.navigation.AppDestination
import cz.novavesodpad.navigation.GlobalCoordinatorImpl
import cz.novavesodpad.navigation.appNavGraph
import cz.novavesodpad.ui.theme.NovaVesOdpadTheme

/**
 * Main activity for the app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaVesOdpadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val globalCoordinator = GlobalCoordinatorImpl(this, navController)
                    
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.Home.route
                    ) {
                        appNavGraph(navController, globalCoordinator)
                    }
                }
            }
        }
    }
}