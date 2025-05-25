package com.mugeaters.popelnice.nvpp.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.ui.theme.LocalAppColors
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen that allows configuring notification preferences - redesigned to match iOS version
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    days: List<TrashDay> = emptyList(),
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current
    
    // Set up permission launcher to handle permission results
    val permissionLauncher = rememberNotificationPermissionLauncher { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }
    
    // Check permission status and request permission immediately on first load
    LaunchedEffect(Unit) {
        viewModel.checkNotificationPermission()
        viewModel.setDays(days)
        // Request notification permission immediately when entering Settings screen
        viewModel.requestNotificationPermissionIfNeeded(permissionLauncher)
    }
    
    // Re-check permission when returning from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                
                // Notification permission warning - show when permissions not authorized AND user has tried to enable notifications (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.notificationsAuthorized && state.permissionExplicitlyDenied) {
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
                                    viewModel.openSystemSettings()
                                }
                            ) {
                                Text("Nastavení")
                            }
                        }
                    }
                }
                
                // Scheduling progress indicator - show when notifications are being scheduled
                if (state.schedulingNotificationsInProgress) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Plánuji notifikace...",
                                color = appColors.regularText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = appColors.regularText,
                                strokeWidth = 2.dp
                            )
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
                        onEnabledChanged = { enabled -> 
                            viewModel.setNotificationEnabledThreeDaysBefore(enabled, permissionLauncher) 
                        },
                        onHourSelected = viewModel::setSelectedNotificationHourThreeDaysBefore,
                        permissionsAuthorized = state.notificationsAuthorized
                    )
                }
                
                // Two days before
                item {
                    NotificationSetupItem(
                        title = "Dva dny předem",
                        isEnabled = state.notificationEnabledTwoDaysBefore,
                        selectedHour = state.selectedNotificationHourTwoDaysBefore,
                        availableHours = state.notificationHours,
                        onEnabledChanged = { enabled -> 
                            viewModel.setNotificationEnabledTwoDaysBefore(enabled, permissionLauncher) 
                        },
                        onHourSelected = viewModel::setSelectedNotificationHourTwoDaysBefore,
                        permissionsAuthorized = state.notificationsAuthorized
                    )
                }
                
                // One day before
                item {
                    NotificationSetupItem(
                        title = "Jeden den předem",
                        isEnabled = state.notificationEnabledOneDayBefore,
                        selectedHour = state.selectedNotificationHourOneDayBefore,
                        availableHours = state.notificationHours,
                        onEnabledChanged = { enabled -> 
                            viewModel.setNotificationEnabledOneDayBefore(enabled, permissionLauncher) 
                        },
                        onHourSelected = viewModel::setSelectedNotificationHourOneDayBefore,
                        permissionsAuthorized = state.notificationsAuthorized
                    )
                }
                
                // On the day
                item {
                    NotificationSetupItem(
                        title = "V den svozu",
                        isEnabled = state.notificationEnabledOnDay,
                        selectedHour = state.selectedNotificationHourOnDay,
                        availableHours = state.notificationHours,
                        onEnabledChanged = { enabled -> 
                            viewModel.setNotificationEnabledOnDay(enabled, permissionLauncher) 
                        },
                        onHourSelected = viewModel::setSelectedNotificationHourOnDay,
                        permissionsAuthorized = state.notificationsAuthorized
                    )
                }
            }
        }
    }
}