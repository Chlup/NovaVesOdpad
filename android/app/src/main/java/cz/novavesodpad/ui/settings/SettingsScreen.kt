package cz.novavesodpad.ui.settings

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen that allows configuring notification preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Request notification permission if needed (Android 13+)
    val permissionLauncher = rememberNotificationPermissionLauncher { isGranted ->
        if (isGranted) {
            viewModel.checkNotificationPermission()
            viewModel.notificationSettingsChanged()
        }
    }
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.notificationsAuthorized) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nastavení") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Notification permission warning
            if (!state.notificationsAuthorized && state.notificationsEnabledForAnyDay) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Notifikace nejsou povolené pro tuto aplikace, proto nebudou fungovat. " +
                                   "Prosím povolte použití notifikací v nastavení vašeho zařízení.",
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        ) {
                            Text("Jít do nastavení")
                        }
                    }
                    
                    Divider()
                }
            }
            
            // Section header
            item {
                Text(
                    text = "Nastavení notifikací",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            // Three days before
            item {
                NotificationSetupItem(
                    title = "Tři dny předem",
                    isEnabled = state.notificationEnabledThreeDaysBefore,
                    selectedHour = state.selectedNotificationHourThreeDaysBefore,
                    availableHours = state.notificationHours,
                    onEnabledChanged = viewModel::setNotificationEnabledThreeDaysBefore,
                    onHourSelected = viewModel::setSelectedNotificationHourThreeDaysBefore
                )
                
                Divider()
            }
            
            // Two days before
            item {
                NotificationSetupItem(
                    title = "Dva dny předem",
                    isEnabled = state.notificationEnabledTwoDaysBefore,
                    selectedHour = state.selectedNotificationHourTwoDaysBefore,
                    availableHours = state.notificationHours,
                    onEnabledChanged = viewModel::setNotificationEnabledTwoDaysBefore,
                    onHourSelected = viewModel::setSelectedNotificationHourTwoDaysBefore
                )
                
                Divider()
            }
            
            // One day before
            item {
                NotificationSetupItem(
                    title = "Jeden den předem",
                    isEnabled = state.notificationEnabledOneDayBefore,
                    selectedHour = state.selectedNotificationHourOneDayBefore,
                    availableHours = state.notificationHours,
                    onEnabledChanged = viewModel::setNotificationEnabledOneDayBefore,
                    onHourSelected = viewModel::setSelectedNotificationHourOneDayBefore
                )
                
                Divider()
            }
            
            // On the day
            item {
                NotificationSetupItem(
                    title = "V den svozu",
                    isEnabled = state.notificationEnabledOnDay,
                    selectedHour = state.selectedNotificationHourOnDay,
                    availableHours = state.notificationHours,
                    onEnabledChanged = viewModel::setNotificationEnabledOnDay,
                    onHourSelected = viewModel::setSelectedNotificationHourOnDay
                )
            }
        }
    }
}