package cz.novavesodpad.ui.settings

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.novavesodpad.ui.theme.LocalAppColors
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen that allows configuring notification preferences - redesigned to match iOS version
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current
    
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
                title = { 
                    Text(
                        text = "Zpět",
                        color = appColors.regularText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Zpět",
                            tint = appColors.regularText
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.screenBackground)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Title
                item {
                    Text(
                        text = "Nastavení notifikací",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.regularText
                    )
                }
                
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
                    }
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
}