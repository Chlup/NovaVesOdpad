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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mugeaters.popelnice.nvpp.model.NotificationDayOffset
import com.mugeaters.popelnice.nvpp.model.NotificationHour
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
                viewModel.checkExactAlarmPermission()
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
                
                // Permission warning for exact alarms
                if (state.notificationsAuthorized && !state.canScheduleExactAlarms) {
                    item {
                        ExactAlarmPermissionCard(
                            onEnableExactAlarmsClick = viewModel::enableAccurateNotifications
                        )
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
                
                // Single notification setup
                item {
                    SingleNotificationSetupCard(
                        isEnabled = state.notificationEnabled,
                        selectedDaysOffset = state.notificationDaysOffset,
                        selectedHour = state.selectedNotificationHour,
                        availableDayOffsets = state.notificationDayOffsets,
                        availableHours = state.notificationHours,
                        onEnabledChanged = viewModel::setNotificationEnabled,
                        onDaysOffsetSelected = viewModel::setNotificationDaysOffset,
                        onHourSelected = viewModel::setSelectedNotificationHour,
                        permissionsAuthorized = state.notificationsAuthorized
                    )
                }
            }
        }
    }
}

/**
 * Simplified notification setup card for single notification (matching iOS)
 */
@Composable
fun SingleNotificationSetupCard(
    isEnabled: Boolean,
    selectedDaysOffset: Int,
    selectedHour: Int,
    availableDayOffsets: List<NotificationDayOffset>,
    availableHours: List<NotificationHour>,
    onEnabledChanged: (Boolean) -> Unit,
    onDaysOffsetSelected: (Int) -> Unit,
    onHourSelected: (Int) -> Unit,
    permissionsAuthorized: Boolean = true
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            // Enable/disable toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notifikace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.regularText,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = if (permissionsAuthorized) onEnabledChanged else { _ -> },
                    enabled = permissionsAuthorized
                )
            }
            
            // Day offset and hour selectors - only shown if notifications are enabled
            if (isEnabled) {
                // Day offset selector
                DayOffsetSelector(
                    selectedDaysOffset = selectedDaysOffset,
                    availableDayOffsets = availableDayOffsets,
                    onDaysOffsetSelected = onDaysOffsetSelected
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Hour selector
                NotificationTimeSelector(
                    selectedHour = selectedHour,
                    availableHours = availableHours,
                    onHourSelected = onHourSelected
                )
            }
        }
    }
}

/**
 * Day offset selector dropdown
 */
@Composable
fun DayOffsetSelector(
    selectedDaysOffset: Int,
    availableDayOffsets: List<NotificationDayOffset>,
    onDaysOffsetSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOffsetLabel = remember(selectedDaysOffset, availableDayOffsets) {
        availableDayOffsets.find { it.daysOffset == selectedDaysOffset }?.title ?: "V den vývozu"
    }
    val appColors = LocalAppColors.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Kdy upozornit",
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.regularText,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = selectedOffsetLabel,
                color = appColors.regularText
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableDayOffsets.forEach { dayOffset ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = dayOffset.title,
                            color = appColors.regularText
                        ) 
                    },
                    onClick = {
                        onDaysOffsetSelected(dayOffset.daysOffset)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Permission warning card for exact alarms
 */
@Composable
fun ExactAlarmPermissionCard(
    onEnableExactAlarmsClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚠️ Pro přesné doručování notifikací",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Přesné alarmy nejsou povoleny. Notifikace mohou přijít se zpožděním 10-60 minut.",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.regularText
            )
            
            OutlinedButton(
                onClick = onEnableExactAlarmsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Povolit přesné alarmy",
                    color = appColors.regularText
                )
            }
        }
    }
}