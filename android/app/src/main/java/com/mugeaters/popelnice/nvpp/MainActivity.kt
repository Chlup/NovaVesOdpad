package com.mugeaters.popelnice.nvpp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mugeaters.popelnice.nvpp.navigation.AppDestination
import com.mugeaters.popelnice.nvpp.navigation.GlobalCoordinatorImpl
import com.mugeaters.popelnice.nvpp.navigation.appNavGraph
import com.mugeaters.popelnice.nvpp.ui.theme.NovaVesOdpadTheme

/**
 * Main activity for the app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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