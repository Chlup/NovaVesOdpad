package com.mugeaters.popelnice.nvpp.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mugeaters.popelnice.nvpp.model.NotificationHour
import com.mugeaters.popelnice.nvpp.model.NotificationDayOffset
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.service.NotificationBuilderInput
import com.mugeaters.popelnice.nvpp.service.NotificationsBuilder
import com.mugeaters.popelnice.nvpp.util.Logger
import com.mugeaters.popelnice.nvpp.util.TasksManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewState for the Settings screen
 */
data class SettingsState(
    val notificationsAuthorized: Boolean = false,
    val permissionExplicitlyDenied: Boolean = false,
    val shouldShowSystemSettingsPrompt: Boolean = false,
    val notificationHours: List<NotificationHour> = NotificationHour.createDefaultHours(),
    val notificationDayOffsets: List<NotificationDayOffset> = NotificationDayOffset.createDefaultOptions(),
    
    val notificationEnabled: Boolean = false,
    val notificationDaysOffset: Int = 0,
    val selectedNotificationHour: Int = 8,
    
    val days: List<TrashDay> = emptyList(),
    val schedulingNotificationsInProgress: Boolean = false
)

/**
 * ViewModel for the Settings screen
 */
class SettingsViewModel(
    private val context: Context,
    private val notificationsBuilder: NotificationsBuilder,
    private val preferencesManager: PreferencesManager,
    private val tasksManager: TasksManager,
    private val logger: Logger
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    private val scheduleNotificationsTaskId = "schedule_notifications_task"
    
    init {
        logger.debug("⚙️ SettingsViewModel initialized")
        loadSettings()
    }
    
    /**
     * Loads saved notification settings from preferences
     */
    fun loadSettings() {
        val settings = preferencesManager.getNotificationSettings()
        _state.update { 
            it.copy(
                notificationEnabled = settings.notificationEnabled,
                notificationDaysOffset = settings.notificationDaysOffset,
                selectedNotificationHour = settings.selectedNotificationHour
            )
        }
        
        // Reschedule notifications when settings screen is opened (like iOS app startup)
        logger.debug("⚙️ Settings loaded, triggering notification reschedule")
        rescheduleNotificationsIfNeeded()
    }
    
    /**
     * Sets the list of trash days (usually passed from the Home screen)
     */
    fun setDays(days: List<TrashDay>) {
        _state.update { it.copy(days = days) }
    }
    
    /**
     * Checks if notification permission is granted
     */
    fun checkNotificationPermission() {
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Before Android 13, notification permission was granted by default
        }
        
        _state.update { 
            it.copy(
                notificationsAuthorized = permissionGranted,
                // Reset denial flag if permission is now granted (user went to settings and enabled it)
                permissionExplicitlyDenied = if (permissionGranted) false else it.permissionExplicitlyDenied
            ) 
        }
    }
    
    /**
     * Requests notification permission immediately when entering Settings screen if not already granted
     */
    fun requestNotificationPermissionIfNeeded(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>?) {
        val currentState = state.value
        if (!currentState.notificationsAuthorized && !currentState.permissionExplicitlyDenied) {
            requestNotificationPermission(permissionLauncher)
        }
    }
    
    /**
     * Requests notification permission when needed
     */
    fun requestNotificationPermission(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // For older Android versions, permission is automatically granted
            _state.update { it.copy(notificationsAuthorized = true) }
            notificationSettingsChanged()
        }
    }
    
    
    /**
     * Opens system settings for notification permissions
     */
    fun openSystemSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            logger.error("Failed to open system settings", e)
        }
    }
    
    /**
     * Called when permission is granted or denied
     */
    fun onPermissionResult(granted: Boolean) {
        _state.update { 
            it.copy(
                notificationsAuthorized = granted,
                permissionExplicitlyDenied = !granted
            ) 
        }
        if (granted) {
            // Permission granted, now save settings and schedule notifications
            notificationSettingsChanged()
        }
        // Don't automatically disable notifications when permission is denied
        // Let the user keep their settings and show warning message instead
    }
    
    /**
     * Called when notification settings are changed
     */
    fun notificationSettingsChanged() {
        val currentState = state.value
        
        logger.debug("📝 Notification settings changed: enabled=${currentState.notificationEnabled}, offset=${currentState.notificationDaysOffset}, hour=${currentState.selectedNotificationHour}")
        
        // Save settings to preferences (even if permission denied - user can see warning)
        preferencesManager.saveNotificationSettings(
            NotificationSettings(
                notificationEnabled = currentState.notificationEnabled,
                notificationDaysOffset = currentState.notificationDaysOffset,
                selectedNotificationHour = currentState.selectedNotificationHour
            )
        )
        
        // Always schedule/cancel notifications based on current settings
        // The scheduleNotifications() method handles permissions internally
        scheduleNotifications()
    }
    
    /**
     * Sets whether notifications are enabled
     */
    fun setNotificationEnabled(enabled: Boolean) {
        logger.debug("🔧 User changed notification enabled to: $enabled")
        _state.update { it.copy(notificationEnabled = enabled) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification day offset (0 = on day, 1 = one day before, etc.)
     */
    fun setNotificationDaysOffset(daysOffset: Int) {
        logger.debug("🔧 User changed notification days offset to: $daysOffset")
        _state.update { it.copy(notificationDaysOffset = daysOffset) }
        notificationSettingsChanged()
    }
    
    /**
     * Sets the notification hour
     */
    fun setSelectedNotificationHour(hour: Int) {
        logger.debug("🔧 User changed notification hour to: $hour")
        _state.update { it.copy(selectedNotificationHour = hour) }
        notificationSettingsChanged()
    }
    
    /**
     * Manually reschedules all notifications (like iOS app startup behavior)
     * This can be called when the settings screen is opened to ensure notifications are up to date
     */
    fun rescheduleNotificationsIfNeeded() {
        val currentState = state.value
        logger.debug("🔄 Checking if notifications need rescheduling: authorized=${currentState.notificationsAuthorized}, days=${currentState.days.size}")
        
        if (currentState.notificationsAuthorized && currentState.days.isNotEmpty()) {
            logger.debug("🔄 Triggering notification reschedule in background")
            // Launch on background thread to avoid blocking UI
            viewModelScope.launch {
                try {
                    executeScheduleNotifications(currentState)
                } catch (e: Exception) {
                    logger.error("Failed to reschedule notifications", e)
                }
            }
        } else {
            logger.debug("🔄 Skipping notification reschedule: not authorized or no days available")
        }
    }
    
    /**
     * Schedules notifications based on current settings
     * Thread-safe implementation that cancels previous scheduling task before starting a new one
     */
    private fun scheduleNotifications() {
        logger.debug("🔄 Starting notification scheduling with progress indicator")
        
        // Show progress indicator immediately on main thread
        _state.update { it.copy(schedulingNotificationsInProgress = true) }
        
        // Launch scheduling on IO dispatcher to avoid blocking UI
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Small delay to allow UI to update with progress indicator
                delay(50)
                
                // Cancel any previous scheduling task and wait for completion
                tasksManager.cancelTaskAndWait(scheduleNotificationsTaskId)
                
                // Execute the actual scheduling on IO thread
                executeScheduleNotifications(state.value)
                
                logger.debug("🔄 Notification scheduling completed")
            } catch (e: Exception) {
                logger.error("Failed to schedule notifications", e)
            } finally {
                // Hide progress indicator on main thread
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(schedulingNotificationsInProgress = false) }
                    logger.debug("🔄 Progress indicator hidden")
                }
            }
        }
    }
    
    private suspend fun executeScheduleNotifications(currentState: SettingsState) {
        // If no permissions or no days, cancel all notifications and return
        if (!currentState.notificationsAuthorized || currentState.days.isEmpty()) {
            notificationsBuilder.cancelAll()
            return
        }
        
        // If notifications are not enabled, cancel all notifications
        if (!currentState.notificationEnabled) {
            notificationsBuilder.cancelAll()
            return
        }

        logger.debug("Running notifications schedule")
        val input = NotificationBuilderInput(
            days = currentState.days,
            notificationEnabled = currentState.notificationEnabled,
            notificationDaysOffset = currentState.notificationDaysOffset,
            selectedNotificationHour = currentState.selectedNotificationHour
        )
        
        notificationsBuilder.build(input)
    }
    
    override fun onCleared() {
        super.onCleared()
        tasksManager.cancelTask(scheduleNotificationsTaskId)
    }
}

/**
 * Data class for storing notification settings
 */
data class NotificationSettings(
    val notificationEnabled: Boolean = false,
    val notificationDaysOffset: Int = 0,
    val selectedNotificationHour: Int = 8
)

/**
 * Interface for managing preferences
 */
interface PreferencesManager {
    fun getNotificationSettings(): NotificationSettings
    fun saveNotificationSettings(settings: NotificationSettings)
}

/**
 * Implementation of preferences manager that uses SharedPreferences
 */
class SharedPreferencesManager(context: Context) : PreferencesManager {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    override fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            notificationEnabled = preferences.getBoolean(KEY_NOTIF_ENABLED, false),
            notificationDaysOffset = preferences.getInt(KEY_NOTIF_DAYS_OFFSET, 0),
            selectedNotificationHour = preferences.getInt(KEY_NOTIF_HOUR, 8)
        )
    }
    
    override fun saveNotificationSettings(settings: NotificationSettings) {
        preferences.edit().apply {
            putBoolean(KEY_NOTIF_ENABLED, settings.notificationEnabled)
            putInt(KEY_NOTIF_DAYS_OFFSET, settings.notificationDaysOffset)
            putInt(KEY_NOTIF_HOUR, settings.selectedNotificationHour)
        }.apply()
    }
    
    companion object {
        private const val PREFERENCES_NAME = "nova_ves_odpad_prefs"
        
        private const val KEY_NOTIF_ENABLED = "notification_enabled"
        private const val KEY_NOTIF_DAYS_OFFSET = "notification_days_offset"
        private const val KEY_NOTIF_HOUR = "notification_hour"
    }
}

/**
 * Composable function that returns a permission request launcher for notification permission
 */
@Composable
fun rememberNotificationPermissionLauncher(
    onPermissionResult: (Boolean) -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )
}